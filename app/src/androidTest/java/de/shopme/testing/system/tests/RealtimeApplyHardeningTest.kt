package de.shopme.testing.system.tests

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.shopme.data.datasource.room.ShopMeDatabase
import de.shopme.data.sync.RemoteApplyCoordinator
import de.shopme.data.sync.queue.ChangeQueueEntity
import de.shopme.data.sync.telemetry.SyncTelemetryCollector
import de.shopme.domain.model.ShoppingItemEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RealtimeApplyHardeningTest {

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
    // STALE REMOTE OVERWRITE BLOCKED
    // ============================================================

    @Test
    fun staleRemoteOverwrite_isBlocked() = runBlocking {

        val local =
            ShoppingItemEntity(
                id = "item-1",
                listId = "list-1",
                name = "Local New",
                quantity = 1,
                category = "Test",
                isChecked = false,
                deletedAt = null,
                createdAt = 100L,
                updatedAt = 200L
            )

        database.itemDao().upsert(local)

        val staleRemote =
            ShoppingItemEntity(
                id = "item-1",
                listId = "list-1",
                name = "Remote Old",
                quantity = 1,
                category = "Test",
                isChecked = false,
                deletedAt = null,
                createdAt = 100L,
                updatedAt = 150L
            )

        remoteApplyCoordinator
            .applyRemoteItem(staleRemote)

        val final =
            database.itemDao()
                .getById("item-1")
                ?: throw AssertionError("Missing item")

        assertEquals(
            "Local New",
            final.name
        )

        assertEquals(
            200L,
            final.updatedAt
        )
    }

    // ============================================================
    // TEST 2
    // LOCAL PENDING MUTATION PROTECTED
    // ============================================================

    @Test
    fun localPendingMutation_isProtected() = runBlocking {

        val local =
            ShoppingItemEntity(
                id = "item-2",
                listId = "list-1",
                name = "Pending Local",
                quantity = 1,
                category = "Test",
                isChecked = false,
                deletedAt = null,
                createdAt = 100L,
                updatedAt = 300L
            )

        database.itemDao().upsert(local)

        database.changeQueueDao().insert(
            ChangeQueueEntity(
                id = "queue-1",
                entityType = "item",
                entityId = "item-2",
                listId = "list-1",
                operation = "UPDATE",
                payload = null,
                createdAt = System.currentTimeMillis(),
                state = "PENDING",
                progress = 0f,
                retryCount = 0,
                lastAttemptAt = null,
                nextRetryAt = null,
                baseVersion = 200L
            )
        )

        val remote =
            ShoppingItemEntity(
                id = "item-2",
                listId = "list-1",
                name = "Remote Attempt",
                quantity = 1,
                category = "Test",
                isChecked = false,
                deletedAt = null,
                createdAt = 100L,
                updatedAt = 250L
            )

        remoteApplyCoordinator
            .applyRemoteItem(remote)

        val final =
            database.itemDao()
                .getById("item-2")
                ?: throw AssertionError("Missing item")

        assertEquals(
            "Pending Local",
            final.name
        )

        assertEquals(
            300L,
            final.updatedAt
        )
    }

    // ============================================================
    // TEST 3
    // DUPLICATE SNAPSHOT SUPPRESSED
    // ============================================================

    @Test
    fun duplicateSnapshot_isIgnored() = runBlocking {

        val remote =
            ShoppingItemEntity(
                id = "item-3",
                listId = "list-1",
                name = "Remote",
                quantity = 1,
                category = "Test",
                isChecked = false,
                deletedAt = null,
                createdAt = 100L,
                updatedAt = 400L
            )

        remoteApplyCoordinator
            .applyRemoteItem(remote)

        remoteApplyCoordinator
            .applyRemoteItem(remote)

        val final =
            database.itemDao()
                .getById("item-3")
                ?: throw AssertionError("Missing item")

        assertEquals(
            "Remote",
            final.name
        )

        assertEquals(
            400L,
            final.updatedAt
        )
    }

    // ============================================================
    // TEST 4
    // REORDERED REMOTE EVENTS IGNORED
    // ============================================================

    @Test
    fun reorderedRemoteEvents_areIgnored() = runBlocking {

        val newest =
            ShoppingItemEntity(
                id = "item-4",
                listId = "list-1",
                name = "Newest",
                quantity = 1,
                category = "Test",
                isChecked = false,
                deletedAt = null,
                createdAt = 100L,
                updatedAt = 500L
            )

        val stale =
            ShoppingItemEntity(
                id = "item-4",
                listId = "list-1",
                name = "Old",
                quantity = 1,
                category = "Test",
                isChecked = false,
                deletedAt = null,
                createdAt = 100L,
                updatedAt = 300L
            )

        remoteApplyCoordinator
            .applyRemoteItem(newest)

        remoteApplyCoordinator
            .applyRemoteItem(stale)

        val final =
            database.itemDao()
                .getById("item-4")
                ?: throw AssertionError("Missing item")

        assertEquals(
            "Newest",
            final.name
        )

        assertEquals(
            500L,
            final.updatedAt
        )
    }

    // ============================================================
    // TEST 5
    // REMOTE DELETE HAS PRIORITY
    // ============================================================

    @Test
    fun remoteDelete_hasPriority() = runBlocking {

        val local =
            ShoppingItemEntity(
                id = "item-5",
                listId = "list-1",
                name = "Alive",
                quantity = 1,
                category = "Test",
                isChecked = false,
                deletedAt = null,
                createdAt = 100L,
                updatedAt = 100L
            )

        database.itemDao().upsert(local)

        val deletedRemote =
            local.copy(
                deletedAt = 999L,
                updatedAt = 999L
            )

        remoteApplyCoordinator
            .applyRemoteItem(deletedRemote)

        val final =
            database.itemDao()
                .getById("item-5")
                ?: throw AssertionError("Missing item")

        assertTrue(
            final.deletedAt != null
        )

        assertEquals(
            999L,
            final.updatedAt
        )
    }
}