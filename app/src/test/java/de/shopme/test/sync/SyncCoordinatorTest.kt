package de.shopme.test.sync

import com.google.gson.Gson
import de.shopme.core.AppScope
import de.shopme.data.sync.SyncCoordinator
import de.shopme.data.sync.ChangeQueueEntity
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.test.fakes.*
import de.shopme.test.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SyncCoordinatorTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    // ============================================================
    // ✅ SUCCESS CASE
    // ============================================================

    @Test
    fun `given create list when success then marked done`()  = runTest {

        val queueDao = FakeChangeQueueDao()
        val listDao = FakeListDao()
        val itemDao = FakeItemDao()
        val firestore = FakeFirestoreDataSource()

        val coordinator = SyncCoordinator(
            changeQueueDao = queueDao,
            itemDao = itemDao,
            listDao = listDao,
            firestore = firestore,
            appScope = AppScope(),
            firebaseAuth = fakeAuth("user1")
        )

        val list = ShoppingListEntity(
            id = "list1",
            name = "Test",
            ownerId = "",
            storeTypes = emptyList(),
            itemCount = 0,
            createdAt = 0,
            updatedAt = 0
        )

        queueDao.insert(
            ChangeQueueEntity(
                id = "1",
                entityType = "list",
                entityId = "list1",
                listId = "list1",
                operation = "CREATE",
                payload = Gson().toJson(list),
                createdAt = 0,
                state = "PENDING",
                progress = 0f,
                baseVersion = 0L
            )
        )

        coordinator.start()
        advanceUntilIdle()

        assertTrue(queueDao.queue.none { it.state == "PENDING" })
    }

    // ============================================================
    // ❌ PERMISSION ERROR
    // ============================================================

    @Test
    fun `given create list when permission denied then retry`()  = runTest {

        val queueDao = FakeChangeQueueDao()
        val firestore = FakeFirestoreDataSource().apply {
            failMode = "PERMISSION"
        }

        val coordinator = SyncCoordinator(
            changeQueueDao = queueDao,
            itemDao = FakeItemDao(),
            listDao = FakeListDao(),
            firestore = firestore,
            appScope = AppScope(),
            firebaseAuth = fakeAuth("user1")
        )

        val list = ShoppingListEntity(
            id = "list1",
            name = "Test",
            ownerId = "",
            storeTypes = emptyList(),
            itemCount = 0,
            createdAt = 0,
            updatedAt = 0
        )

        queueDao.insert(
            ChangeQueueEntity(
                id = "1",
                entityType = "list",
                entityId = "list1",
                listId = "list1",
                operation = "CREATE",
                payload = Gson().toJson(list),
                createdAt = 0,
                state = "PENDING",
                progress = 0f,
                baseVersion = 0L
            )
        )

        coordinator.start()
        advanceUntilIdle()

        assertTrue(queueDao.queue.any { it.state == "PENDING" })
    }

    // ============================================================
    // 🌐 NETWORK ERROR
    // ============================================================

    @Test
    fun `given create list when network error then retry`()  = runTest {

        val queueDao = FakeChangeQueueDao()
        val firestore = FakeFirestoreDataSource().apply {
            failMode = "NETWORK"
        }

        val coordinator = SyncCoordinator(
            changeQueueDao = queueDao,
            itemDao = FakeItemDao(),
            listDao = FakeListDao(),
            firestore = firestore,
            appScope = AppScope(),
            firebaseAuth = fakeAuth("user1")
        )

        val list = ShoppingListEntity(
            id = "list1",
            name = "Test",
            ownerId = "",
            storeTypes = emptyList(),
            itemCount = 0,
            createdAt = 0,
            updatedAt = 0
        )

        queueDao.insert(
            ChangeQueueEntity(
                id = "1",
                entityType = "list",
                entityId = "list1",
                listId = "list1",
                operation = "CREATE",
                payload = Gson().toJson(list),
                createdAt = 0,
                state = "PENDING",
                progress = 0f,
                baseVersion = 0L
            )
        )

        coordinator.start()
        advanceUntilIdle()

        assertTrue(queueDao.queue.any { it.state == "PENDING" })
    }

    // ============================================================
    // 🚫 INVALID PAYLOAD
    // ============================================================

    @Test
    fun `given invalid payload when processing then no crash`()  = runTest {

        val queueDao = FakeChangeQueueDao()

        val coordinator = SyncCoordinator(
            changeQueueDao = queueDao,
            itemDao = FakeItemDao(),
            listDao = FakeListDao(),
            firestore = FakeFirestoreDataSource(),
            appScope = AppScope(),
            firebaseAuth = fakeAuth("user1")
        )

        queueDao.insert(
            ChangeQueueEntity(
                id = "1",
                entityType = "list",
                entityId = "list1",
                listId = "list1",
                operation = "CREATE",
                payload = "INVALID_JSON",
                createdAt = 0,
                state = "PENDING",
                progress = 0f,
                baseVersion = 0L
            )
        )

        coordinator.start()
        advanceUntilIdle()

        // Wichtig: KEIN CRASH → Test besteht wenn er durchläuft
        assertTrue(true)
    }
}