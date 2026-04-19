package de.shopme.test.usecase

import de.shopme.data.repository.RoomShoppingRepository
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.domain.usecase.CreateListUseCase
import de.shopme.presentation.action.ShoppingAction

import de.shopme.test.fakes.FakeChangeQueueDao
import de.shopme.test.fakes.FakeItemDao
import de.shopme.test.fakes.FakeListDao
import de.shopme.test.utils.MainDispatcherRule
import de.shopme.test.utils.createTestViewModel

import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertFalse


@OptIn(ExperimentalCoroutinesApi::class)
class CreateListUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun createList_inserts_into_room() = runTest {

        val listDao = FakeListDao()
        val itemDao = FakeItemDao()
        val queueDao = FakeChangeQueueDao()

        val repo = RoomShoppingRepository(
            listDao = listDao,
            itemDao = itemDao,
            changeQueueDao = queueDao,
            firestoreDataSource = mockk(relaxed = true)
        )

        val useCase = CreateListUseCase(repo)

        val listId = useCase("Test", emptyList())

        val stored = listDao.getById(listId)

        assertNotNull(stored)
    }

    @Test
    fun deleteList_writes_delete_to_queue() = runTest {

        val listDao = FakeListDao()
        val itemDao = FakeItemDao()
        val queueDao = FakeChangeQueueDao()

        val repo = RoomShoppingRepository(
            listDao = listDao,
            itemDao = itemDao,
            changeQueueDao = queueDao,
            firestoreDataSource = mockk(relaxed = true)
        )

        val listId = "list1"

        listDao.upsert(
            ShoppingListEntity(
                id = listId,
                name = "Test",
                ownerId = "",
                storeTypes = emptyList(),
                itemCount = 0,
                createdAt = 0,
                updatedAt = 0
            )
        )

        repo.deleteList(listId)

        assertTrue(queueDao.queue.any {
            it.operation == "DELETE" && it.entityId == listId
        })
    }

    @Test
    fun repository_createList_writes_queue() = runTest {

        val listDao = FakeListDao()
        val itemDao = FakeItemDao()
        val queueDao = FakeChangeQueueDao()

        val repo = RoomShoppingRepository(
            listDao = listDao,
            itemDao = itemDao,
            changeQueueDao = queueDao,
            firestoreDataSource = mockk(relaxed = true)
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

        repo.createList(list)

        assertTrue(queueDao.queue.any {
            it.operation == "CREATE" && it.entityType == "list"
        })
    }

    @Test
    fun invite_load_sets_state_correctly() = runTest {

        val vm = createTestViewModel()

        vm.handleInviteFlow("invite123")

        advanceUntilIdle()

        val state = vm.state.value

        assertTrue(state.showInviteDialog)
        assertNotNull(state.inviteListIds)
        assertNotNull(state.inviteSenderName)
    }

    @Test
    fun invite_invalid_sets_error() = runTest {

        val vm = createTestViewModel()

        vm.handleInviteFlow("invalid") // 🔥 FIX

        advanceUntilIdle()

        val state = vm.state.value

        assertNotNull(state.inviteError)
    }

    @Test
    fun accept_invite_sets_joining_state() = runTest {

        val queueDao = FakeChangeQueueDao()
        val vm = createTestViewModel(queueDao)

        vm.handleInviteFlow("invite123")

        advanceUntilIdle() // 🔥 CRITICAL

        vm.acceptCurrentInvite()

        advanceUntilIdle()

        val state = vm.state.value

        assertFalse(state.isJoining)
    }

    @Test
    fun resolve_invite_lists_maps_correctly() = runTest {

        val queueDao = FakeChangeQueueDao()
        val vm = createTestViewModel(queueDao)

        vm.handleInviteFlow("invite123")

        advanceUntilIdle()

        val state = vm.state.value

        assertNotNull(state.inviteResolvedLists)
    }

    @Test
    fun invite_expired_sets_error() = runTest {

        val vm = createTestViewModel()

        vm.handleInviteFlow("expired")

        advanceUntilIdle()

        val state = vm.state.value

        assertNotNull(state.inviteError)
    }

    @Test
    fun invite_used_sets_error() = runTest {

        val vm = createTestViewModel()

        vm.handleInviteFlow("used")

        advanceUntilIdle()

        val state = vm.state.value

        assertNotNull(state.inviteError)
    }
}