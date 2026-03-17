package de.shopme.domain.usecase

import de.shopme.data.repository.RoomShoppingRepository

class DeleteListUseCase(
    private val roomRepository: RoomShoppingRepository
) {

    suspend operator fun invoke(listId: String) {

        roomRepository.deleteList(listId)
    }
}