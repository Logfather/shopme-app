package de.shopme.testing.system.tests

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.shopme.data.datasource.room.ShopMeDatabase
import de.shopme.data.repository.RoomShoppingRepository
import de.shopme.data.sync.ConflictResolver
import de.shopme.data.sync.RemoteApplyCoordinator
import de.shopme.data.sync.SyncCoordinator
import de.shopme.data.sync.SyncResult
import de.shopme.data.sync.telemetry.SyncTelemetryCollector
import de.shopme.domain.life.NimelisEventBus
import de.shopme.testing.system.fake.FakeFirestoreGateway
import de.shopme.testing.system.scenario.EventDrivenReplayScenario
import de.shopme.testing.system.tests.sync.TestRuntimeDiagnostics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EventDrivenReplayRuntimeTest {

    private lateinit var database: ShopMeDatabase

    private lateinit var firestore: FakeFirestoreGateway

    private lateinit var coordinator: SyncCoordinator

    private lateinit var scenario: EventDrivenReplayScenario

    private lateinit var repository: RoomShoppingRepository

    private lateinit var appScope: CoroutineScope

    private lateinit var eventBus: NimelisEventBus

    @Before
    fun setup() {

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ShopMeDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        firestore = FakeFirestoreGateway()

        eventBus =
            NimelisEventBus()

        appScope =
            CoroutineScope(Dispatchers.IO)

        repository =
            RoomShoppingRepository(
                itemDao = database.itemDao(),
                listDao = database.listDao(),
                changeQueueDao = database.changeQueueDao(),
                firestoreDataSource = firestore,
                nimelisEventBus = eventBus
            )

        val telemetry = SyncTelemetryCollector()

        val remoteApplyCoordinatorB =
            RemoteApplyCoordinator(
                itemDao = database.itemDao(),
                changeQueueDao = database.changeQueueDao(),
                remoteApplyStateDao =
                    database.remoteApplyStateDao(),
                telemetry = telemetry
            )

        coordinator =
            SyncCoordinator(
                changeQueueDao = database.changeQueueDao(),
                itemDao = database.itemDao(),
                listDao = database.listDao(),
                firestore = firestore,
                appScope = appScope,
                firebaseAuth = null,
                conflictResolver = ConflictResolver(),
                roomRepository = repository,
                remoteApplyCoordinator = remoteApplyCoordinatorB,
                telemetry = telemetry,

                diagnosticsProvider =
                    TestRuntimeDiagnostics.provider(
                        telemetry
                    ),

                diagnosticsLogger =
                    TestRuntimeDiagnostics.logger()
            )

        scenario =
            EventDrivenReplayScenario(
                database = database,
                firestore = firestore
            )
    }

    @After
    fun teardown() {

        appScope.cancel()

        database.close()
    }

    @Test
    fun triggerSync_processesQueuedChanges() = runTest {

        val item =
            scenario.enqueueCreateItem()

        val result =
            coordinator.triggerSync()

        assertEquals(
            SyncResult.Success,
            result
        )

        val remote =
            firestore.getItem(
                item.listId,
                item.id
            )

        assertNotNull(remote)

        val queue =
            database
                .changeQueueDao()
                .getPending(limit = 100)

        assertTrue(
            queue.isEmpty()
        )
    }

    @Test
    fun multipleTriggerSyncCalls_areSerialized() = runTest {

        val item =
            scenario.enqueueCreateItem()

        val jobs =
            (1..10).map {

                async {
                    coordinator.triggerSync()
                }
            }

        jobs.awaitAll()

        val remote =
            firestore.getItem(
                item.listId,
                item.id
            )

        assertNotNull(remote)

        val pending =
            database
                .changeQueueDao()
                .getPending(limit = 100)

        assertTrue(
            pending.isEmpty()
        )
    }

    @Test
    fun queueOrder_isPreserved() = runTest {

        val item =
            scenario.enqueueCreateUpdateDeleteSequence()

        val pendingBeforeSync =
            database
                .changeQueueDao()
                .getPending(limit = 100)

        // ============================================================
        // State-compacted replay queue:
        // CREATE -> UPDATE -> DELETE
        // collapses into a single DELETE operation.
        // ============================================================

        assertEquals(
            1,
            pendingBeforeSync.size
        )

        assertEquals(
            "DELETE",
            pendingBeforeSync.first().operation
        )

        coordinator.triggerSync()

        val remote =
            firestore.getItem(
                item.listId,
                item.id
            )

        // ============================================================
        // Current architecture performs HARD DELETE.
        // Item must no longer exist remotely.
        // ============================================================

        assertEquals(
            null,
            remote
        )

        val pendingAfterSync =
            database
                .changeQueueDao()
                .getPending(limit = 100)

        assertTrue(
            pendingAfterSync.isEmpty()
        )
    }

    @Test
    fun retryBackoff_preventsImmediateReplay() = runTest {

        scenario.enqueueRetryItem()

        coordinator.triggerSync()

        val pending =
            database
                .changeQueueDao()
                .getPending(limit = 100)

        assertEquals(
            1,
            pending.size
        )

        val retryItem =
            pending.first()

        assertTrue(
            retryItem.retryCount > 0
        )

        val beforeRetry =
            retryItem.lastAttemptAt

        coordinator.triggerSync()

        val afterRetry =
            database
                .changeQueueDao()
                .getPending(limit = 100)
                .first()

        assertEquals(
            beforeRetry,
            afterRetry.lastAttemptAt
        )
    }
}