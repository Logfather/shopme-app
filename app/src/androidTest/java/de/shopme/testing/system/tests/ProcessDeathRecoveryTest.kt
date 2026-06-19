package de.shopme.data.sync

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.shopme.data.datasource.room.ShopMeDatabase
import de.shopme.data.repository.RoomShoppingRepository
import de.shopme.data.sync.queue.ChangeQueueDao
import de.shopme.data.sync.queue.ChangeQueueEntity
import de.shopme.data.sync.runtime.ListenerActivationRegistry
import de.shopme.data.sync.runtime.SyncBootstrapper
import de.shopme.data.sync.runtime.SyncRuntimeStateHolder
import de.shopme.data.sync.telemetry.SyncTelemetryCollector
import de.shopme.domain.life.NimelisEventBus
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.testing.system.fake.FakeFirestoreGateway
import de.shopme.testing.system.tests.sync.TestRuntimeDiagnostics
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.UUID

class ProcessDeathRecoveryTest {

    private lateinit var database: ShopMeDatabase

    private lateinit var changeQueueDao: ChangeQueueDao

    private lateinit var firestore: FakeFirestoreGateway

    private lateinit var syncCoordinator: SyncCoordinator

    private lateinit var firestoreListener: FirestoreListener

    private lateinit var bootstrapper: SyncBootstrapper

    private lateinit var runtimeStateHolder: SyncRuntimeStateHolder

    private lateinit var listenerRegistry: ListenerActivationRegistry

    private lateinit var roomRepository: RoomShoppingRepository

    private val scope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.IO
        )

    @Before
    fun setup() {

        database =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                ShopMeDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()

        changeQueueDao =
            database.changeQueueDao()

        firestore = FakeFirestoreGateway()

        roomRepository =
            RoomShoppingRepository(
                itemDao = database.itemDao(),
                listDao = database.listDao(),
                changeQueueDao = changeQueueDao,
                firestoreDataSource = firestore,
                nimelisEventBus = NimelisEventBus()
            )

        val telemetry = SyncTelemetryCollector()

        val remoteApplyCoordinator =
            RemoteApplyCoordinator(
                itemDao = database.itemDao(),
                changeQueueDao = changeQueueDao,
                remoteApplyStateDao =
                    database.remoteApplyStateDao(),
                telemetry = telemetry
            )

        syncCoordinator =
            SyncCoordinator(
                changeQueueDao = database.changeQueueDao(),
                itemDao = database.itemDao(),
                listDao = database.listDao(),
                firestore = firestore,
                appScope = scope,
                firebaseAuth = null,
                conflictResolver = ConflictResolver(),
                roomRepository = roomRepository,
                remoteApplyCoordinator = remoteApplyCoordinator,
                telemetry = telemetry,

                diagnosticsProvider =
                    TestRuntimeDiagnostics.provider(
                        telemetry
                    ),

                diagnosticsLogger =
                    TestRuntimeDiagnostics.logger()
            )

        runtimeStateHolder =
            SyncRuntimeStateHolder()

        listenerRegistry =
            ListenerActivationRegistry()

        firestoreListener =
            FirestoreListener(
                dataSource = firestore,
                itemDao = database.itemDao(),
                listDao = database.listDao(),
                conflictResolver = ConflictResolver(),
                appScope = scope
            )

        bootstrapper =
            SyncBootstrapper(
                syncCoordinator = syncCoordinator,
                changeQueueDao = changeQueueDao,
                runtimeStateHolder = runtimeStateHolder,
                listenerRegistry = listenerRegistry,
                firestoreListener = firestoreListener
            )

        firestoreListener.bootstrapper =
            bootstrapper
    }

    @After
    fun teardown() {

        scope.cancel()

        database.close()
    }

    // ============================================================
    // TEST 1
    // ============================================================

    @Test
    fun orphanedSyncingState_isRecovered() = runTest {

        val entity =
            ChangeQueueEntity(
                id = UUID.randomUUID().toString(),
                entityType = "item",
                entityId = "item-1",
                listId = "list-1",
                operation = "UPDATE",
                payload = null,
                createdAt = System.currentTimeMillis(),
                state = "SYNCING",
                baseVersion = 1L
            )

        changeQueueDao.insert(entity)

        bootstrapper.recoverRuntime()

        val recovered =
            changeQueueDao.getChangeById(entity.id)

        assertEquals(
            "PENDING",
            recovered?.state
        )
    }

    // ============================================================
    // TEST 2
    // ============================================================

    @Test
    fun startupReplay_finishesBeforeRealtimeActivation() =
        runTest {

            val queueEntity =
                ChangeQueueEntity(
                    id = UUID.randomUUID().toString(),
                    entityType = "item",
                    entityId = "item-1",
                    listId = "list-1",
                    operation = "CREATE",
                    payload = null,
                    createdAt = System.currentTimeMillis(),
                    state = "PENDING",
                    baseVersion = 0L
                )

            changeQueueDao.insert(queueEntity)

            val local =
                ShoppingItemEntity(
                    id = "item-1",
                    listId = "list-1",
                    name = "Milk",
                    quantity = 1,
                    category = "General",
                    isChecked = false,
                    deletedAt = null,
                    createdAt = 1L,
                    updatedAt = 1L
                )

            database.itemDao().upsert(local)

            bootstrapper.startUserRuntime(
                uid = "user-1"
            )

            assertEquals(
                1,
                firestore.addItemCallCount
            )
        }

    // ============================================================
    // TEST 3
    // ============================================================

    @Test
    fun processDeath_doesNotDuplicateReplay() =
        runTest {

            val queueEntity =
                ChangeQueueEntity(
                    id = UUID.randomUUID().toString(),
                    entityType = "item",
                    entityId = "item-1",
                    listId = "list-1",
                    operation = "CREATE",
                    payload = null,
                    createdAt = System.currentTimeMillis(),
                    state = "SYNCING",
                    baseVersion = 0L
                )

            changeQueueDao.insert(queueEntity)

            val local =
                ShoppingItemEntity(
                    id = "item-1",
                    listId = "list-1",
                    name = "Milk",
                    quantity = 1,
                    category = "General",
                    isChecked = false,
                    deletedAt = null,
                    createdAt = 1L,
                    updatedAt = 1L
                )

            database.itemDao().upsert(local)

            bootstrapper.startUserRuntime(
                uid = "user-1"
            )

            assertEquals(
                1,
                firestore.addItemCallCount
            )

        }

    // ============================================================
    // TEST 4
    // ============================================================

    @Test
    fun killDuringReplay_recoversCorrectly() =
        runTest {

            // --------------------------------------------------------
            // GIVEN
            // Queue entry stuck in SYNCING
            // (simulated process death during replay)
            // --------------------------------------------------------

            val queueEntity =
                ChangeQueueEntity(
                    id = UUID.randomUUID().toString(),
                    entityType = "item",
                    entityId = "crash-item",
                    listId = "list-1",
                    operation = "CREATE",
                    payload = null,
                    createdAt = System.currentTimeMillis(),
                    state = "SYNCING",
                    baseVersion = 0L
                )

            changeQueueDao.insert(queueEntity)

            val local =
                ShoppingItemEntity(
                    id = "crash-item",
                    listId = "list-1",
                    name = "Bread",
                    quantity = 1,
                    category = "General",
                    isChecked = false,
                    deletedAt = null,
                    createdAt = 1L,
                    updatedAt = 1L
                )

            database.itemDao().upsert(local)

            // --------------------------------------------------------
            // WHEN
            // Runtime restarts after process death
            // --------------------------------------------------------

            bootstrapper.startUserRuntime(
                uid = "user-1"
            )

            // --------------------------------------------------------
            // THEN
            // SYNCING recovered -> replay resumed
            // --------------------------------------------------------

            val recovered =
                changeQueueDao.getChangeById(
                    queueEntity.id
                )

            assertEquals(
                "DONE",
                recovered?.state
            )

            // --------------------------------------------------------
            // EXACTLY-ONCE GUARANTEE
            // --------------------------------------------------------

            assertEquals(
                1,
                firestore.addItemCallCount
            )
        }
}