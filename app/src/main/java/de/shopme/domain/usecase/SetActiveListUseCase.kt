package de.shopme.domain.usecase

import de.shopme.data.repository.FirestoreShoppingRepository

class SetActiveListUseCase(
    private val repository: FirestoreShoppingRepository
) {

    suspend operator fun invoke(listId: String) {

        repository.setActiveList(listId)

    }

}