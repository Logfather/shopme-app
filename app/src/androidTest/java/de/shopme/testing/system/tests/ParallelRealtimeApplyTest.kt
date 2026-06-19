package de.shopme.testing.system.tests

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.shopme.data.datasource.room.ShopMeDatabase
import de.shopme.data.sync.RemoteApplyCoordinator
import de.shopme.data.sync.telemetry.SyncTelemetryCollector
import de.shopme.domain.model.ShoppingItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ParallelRealtimeApplyTest {

    private lateinit var database: ShopMeDatabase

    private lateinit var remoteApplyCoordinator:
            RemoteApplyCoordinator

    val telemetry = SyncTelemetryCollector()

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

        remoteApplyCoordinator =
            RemoteApplyCoordinator(
                itemDao = database.itemDao(),
                changeQueueDao = database.changeQueueDao(),
                remoteApplyStateDao =
                    database.remoteApplyStateDao(),
                telemetry = telemetry
            )
    }

    @After
    fun teardown() {
        database.close()
    }

    // ============================================================
    // TEST 1
    // PARALLEL DUPLICATE APPLIES
    // ============================================================

    @Test
    fun parallelDuplicateApply_isDeterministic() = runBlocking {

        val remote =
            ShoppingItemEntity(
                id = "parallel-item-1",
                listId = "list-1",
                name = "Parallel",
                quantity = 1,
                category = "Test",
                isChecked = false,
                deletedAt = null,
                createdAt = 100L,
                updatedAt = 500L
            )

        withContext(Dispatchers.IO) {

            val jobs =
                (1..20).map {

                    async {

                        remoteApplyCoordinator
                            .applyRemoteItem(remote)
                    }
                }

            jobs.awaitAll()
        }

        val final =
            database.itemDao()
                .getById("parallel-item-1")

        assertNotNull(final)

        assertEquals(
            "Parallel",
            final?.name
        )

        assertEquals(
            500L,
            final?.updatedAt
        )
    }

    // ============================================================
    // TEST 2
    // PARALLEL STALE + NEW EVENT
    // ============================================================

    @Test
    fun parallelStaleAndNewEvent_keepsNewest() = runBlocking {

        val stale =
            ShoppingItemEntity(
                id = "parallel-item-2",
                listId = "list-1",
                name = "STALE",
                quantity = 1,
                category = "Test",
                isChecked = false,
                deletedAt = null,
                createdAt = 100L,
                updatedAt = 100L
            )

        val newest =
            ShoppingItemEntity(
                id = "parallel-item-2",
                listId = "list-1",
                name = "NEWEST",
                quantity = 1,
                category = "Test",
                isChecked = false,
                deletedAt = null,
                createdAt = 100L,
                updatedAt = 999L
            )

        withContext(Dispatchers.IO) {

            val jobs =
                listOf(

                    async {
                        remoteApplyCoordinator
                            .applyRemoteItem(stale)
                    },

                    async {
                        remoteApplyCoordinator
                            .applyRemoteItem(newest)
                    }
                )

            jobs.awaitAll()
        }

        val final =
            database.itemDao()
                .getById("parallel-item-2")
                ?: throw AssertionError("Missing item")

        assertEquals(
            "NEWEST",
            final.name
        )

        assertEquals(
            999L,
            final.updatedAt
        )
    }

    // ============================================================
    // TEST 3
    // PARALLEL DELETE + UPDATE
    // ============================================================

    @Test
    fun parallelDeleteAndUpdate_deleteWins() = runBlocking {

        val update =
            ShoppingItemEntity(
                id = "parallel-item-3",
                listId = "list-1",
                name = "UPDATED",
                quantity = 1,
                category = "Test",
                isChecked = false,
                deletedAt = null,
                createdAt = 100L,
                updatedAt = 400L
            )

        val delete =
            ShoppingItemEntity(
                id = "parallel-item-3",
                listId = "list-1",
                name = "UPDATED",
                quantity = 1,
                category = "Test",
                isChecked = false,
                deletedAt = 999L,
                createdAt = 100L,
                updatedAt = 999L
            )

        withContext(Dispatchers.IO) {

            val jobs =
                listOf(

                    async {
                        remoteApplyCoordinator
                            .applyRemoteItem(update)
                    },

                    async {
                        remoteApplyCoordinator
                            .applyRemoteItem(delete)
                    }
                )

            jobs.awaitAll()
        }

        val final =
            database.itemDao()
                .getById("parallel-item-3")
                ?: throw AssertionError("Missing item")

        assertEquals(
            999L,
            final.updatedAt
        )

        assertNotNull(
            final.deletedAt
        )
    }

    // ============================================================
    // TEST 4
    // HIGH CONCURRENCY ORDER STABILITY
    // ============================================================

    @Test
    fun highConcurrencyNewestVersion_survives() = runBlocking {

        withContext(Dispatchers.IO) {

            val jobs =
                (1L..100L).map { version ->

                    async {

                        val remote =
                            ShoppingItemEntity(
                                id = "parallel-item-4",
                                listId = "list-1",
                                name = "Version-$version",
                                quantity = 1,
                                category = "Test",
                                isChecked = false,
                                deletedAt = null,
                                createdAt = 100L,
                                updatedAt = version
                            )

                        remoteApplyCoordinator
                            .applyRemoteItem(remote)
                    }
                }

            jobs.awaitAll()
        }

        val final =
            database.itemDao()
                .getById("parallel-item-4")
                ?: throw AssertionError("Missing item")

        assertEquals(
            100L,
            final.updatedAt
        )

        assertEquals(
            "Version-100",
            final.name
        )
    }

    // ============================================================
    // TEST 5
    // MULTI ENTITY PARALLELISM
    // ============================================================

    @Test
    fun multipleEntities_parallelApply_staysConsistent() = runBlocking {

        withContext(Dispatchers.IO) {

            val jobs =
                (1..50).map { index ->

                    async {

                        val remote =
                            ShoppingItemEntity(
                                id = "entity-$index",
                                listId = "list-1",
                                name = "Item-$index",
                                quantity = index,
                                category = "Test",
                                isChecked = false,
                                deletedAt = null,
                                createdAt = 100L,
                                updatedAt = index.toLong()
                            )

                        remoteApplyCoordinator
                            .applyRemoteItem(remote)
                    }
                }

            jobs.awaitAll()
        }

        val all =
            database.itemDao()
                .getAll()

        assertEquals(
            50,
            all.size
        )

        repeat(50) { index ->

            val entityId =
                "entity-${index + 1}"

            val item =
                database.itemDao()
                    .getById(entityId)
                    ?: throw AssertionError(
                        "Missing entity: $entityId"
                    )

            assertEquals(
                "Item-${index + 1}",
                item.name
            )
        }
    }
}