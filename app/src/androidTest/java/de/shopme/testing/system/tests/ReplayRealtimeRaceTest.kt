package de.shopme.testing.system.tests

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.shopme.data.datasource.room.ShopMeDatabase
import de.shopme.data.repository.RoomShoppingRepository
import de.shopme.data.sync.ConflictResolver
import de.shopme.data.sync.RemoteApplyCoordinator
import de.shopme.data.sync.SyncCoordinator
import de.shopme.data.sync.telemetry.SyncTelemetryCollector
import de.shopme.domain.life.NimelisEventBus
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.testing.system.fake.FakeFirestoreGateway
import de.shopme.testing.system.tests.sync.TestRuntimeDiagnostics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReplayRealtimeRaceTest {

    private lateinit var database:
            ShopMeDatabase

    private lateinit var repository:
            RoomShoppingRepository

    private lateinit var syncCoordinator:
            SyncCoordinator

    private lateinit var remoteApplyCoordinator:
            RemoteApplyCoordinator

    private lateinit var firestore:
            FakeFirestoreGateway

    @Before
    fun setup() {

        database =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                ShopMeDatabase::class.java
            )
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()

        firestore =
            FakeFirestoreGateway()

        val eventBus =
            NimelisEventBus()

        repository =
            RoomShoppingRepository(
                itemDao = database.itemDao(),
                listDao = database.listDao(),
                changeQueueDao = database.changeQueueDao(),
                firestoreDataSource = firestore,
                nimelisEventBus = eventBus
            )

        val telemetry = SyncTelemetryCollector()

        remoteApplyCoordinator =
            RemoteApplyCoordinator(
                itemDao = database.itemDao(),
                changeQueueDao = database.changeQueueDao(),
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
                appScope = kotlinx.coroutines.GlobalScope,
                firebaseAuth = null,
                conflictResolver = ConflictResolver(),
                roomRepository = repository,
                remoteApplyCoordinator = remoteApplyCoordinator,
                telemetry = telemetry,

                diagnosticsProvider =
                    TestRuntimeDiagnostics.provider(
                        telemetry
                    ),

                diagnosticsLogger =
                    TestRuntimeDiagnostics.logger()
            )

        repository.attachSyncCoordinator(
            syncCoordinator
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    // ============================================================
    // TEST 1
    // REPLAY VS STALE REALTIME SNAPSHOT
    // ============================================================

    @Test
    fun replayAndRealtimeRace_localStateSurvives() =
        runBlocking {

            // --------------------------------------------------------
            // INITIAL LOCAL STATE
            // --------------------------------------------------------

            val initial =
                ShoppingItemEntity(
                    id = "race-item-1",
                    listId = "list-1",
                    name = "Initial",
                    quantity = 1,
                    category = "Test",
                    isChecked = false,
                    deletedAt = null,
                    createdAt = 100L,
                    updatedAt = 100L
                )

            database.itemDao()
                .upsert(initial)

            // --------------------------------------------------------
            // LOCAL OPTIMISTIC UPDATE
            // --------------------------------------------------------

            val optimistic =
                initial.copy(
                    name = "LOCAL_NEW",
                    updatedAt = 500L
                )

            repository.updateItem(
                optimistic
            )

            // --------------------------------------------------------
            // STALE REMOTE SNAPSHOT
            // --------------------------------------------------------

            val staleRemote =
                initial.copy(
                    name = "REMOTE_OLD",
                    updatedAt = 200L
                )

            // --------------------------------------------------------
            // PARALLEL EXECUTION
            // --------------------------------------------------------

            withContext(Dispatchers.IO) {

                val jobs =
                    listOf(

                        async {

                            syncCoordinator
                                .triggerSync(force = true)
                        },

                        async {

                            remoteApplyCoordinator
                                .applyRemoteItem(
                                    staleRemote
                                )
                        }
                    )

                jobs.awaitAll()
            }

            // --------------------------------------------------------
            // FINAL STATE
            // --------------------------------------------------------

            val final =
                database.itemDao()
                    .getById("race-item-1")
                    ?: throw AssertionError(
                        "Missing final item"
                    )

            assertEquals(
                "LOCAL_NEW",
                final.name
            )

            assertEquals(
                500L,
                final.updatedAt
            )
        }

    // ============================================================
    // TEST 2
    // REPLAY UPDATE VS SAME-TIME REMOTE UPDATE
    // ============================================================

    @Test
    fun replayAndRealtimeRace_newestVersionWins() =
        runBlocking {

            val initial =
                ShoppingItemEntity(
                    id = "race-item-2",
                    listId = "list-1",
                    name = "Initial",
                    quantity = 1,
                    category = "Test",
                    isChecked = false,
                    deletedAt = null,
                    createdAt = 100L,
                    updatedAt = 100L
                )

            database.itemDao()
                .upsert(initial)

            // --------------------------------------------------------
            // LOCAL UPDATE
            // --------------------------------------------------------

            val local =
                initial.copy(
                    name = "LOCAL",
                    updatedAt = 400L
                )

            repository.updateItem(
                local
            )

            // --------------------------------------------------------
            // NEWER REMOTE UPDATE
            // --------------------------------------------------------

            val remote =
                initial.copy(
                    name = "REMOTE_NEWEST",
                    updatedAt = 999L
                )

            // --------------------------------------------------------
            // PARALLEL EXECUTION
            // --------------------------------------------------------

            withContext(Dispatchers.IO) {

                val jobs =
                    listOf(

                        async {

                            syncCoordinator
                                .triggerSync(force = true)
                        },

                        async {

                            remoteApplyCoordinator
                                .applyRemoteItem(
                                    remote
                                )
                        }
                    )

                jobs.awaitAll()
            }

            // --------------------------------------------------------
            // FINAL STATE
            // --------------------------------------------------------

            val final =
                database.itemDao()
                    .getById("race-item-2")
                    ?: throw AssertionError(
                        "Missing final item"
                    )

            assertEquals(
                "REMOTE_NEWEST",
                final.name
            )

            assertEquals(
                999L,
                final.updatedAt
            )
        }

    // ============================================================
    // TEST 3
    // REPLAY DELETE VS STALE REMOTE UPDATE
    // ============================================================

    @Test
    fun replayDeleteBeats_staleRemoteUpdate() =
        runBlocking {

            val initial =
                ShoppingItemEntity(
                    id = "race-item-3",
                    listId = "list-1",
                    name = "Initial",
                    quantity = 1,
                    category = "Test",
                    isChecked = false,
                    deletedAt = null,
                    createdAt = 100L,
                    updatedAt = 100L
                )

            database.itemDao()
                .upsert(initial)

            // --------------------------------------------------------
            // LOCAL DELETE
            // --------------------------------------------------------

            repository.deleteItem(
                initial
            )

            // --------------------------------------------------------
            // STALE REMOTE UPDATE
            // --------------------------------------------------------

            val staleRemote =
                initial.copy(
                    name = "REMOTE_OLD",
                    updatedAt = 150L
                )

            // --------------------------------------------------------
            // PARALLEL EXECUTION
            // --------------------------------------------------------

            withContext(Dispatchers.IO) {

                val jobs =
                    listOf(

                        async {

                            syncCoordinator
                                .triggerSync(force = true)
                        },

                        async {

                            remoteApplyCoordinator
                                .applyRemoteItem(
                                    staleRemote
                                )
                        }
                    )

                jobs.awaitAll()
            }

            // --------------------------------------------------------
            // FINAL STATE
            // --------------------------------------------------------

            val final =
                database.itemDao()
                    .getById("race-item-3")
                    ?: throw AssertionError(
                        "Missing final item"
                    )

            org.junit.Assert.assertNotNull(
                final.deletedAt
            )
        }
}