package de.shopme.domain.usecase

import de.shopme.data.repository.FirestoreShoppingRepository
import de.shopme.domain.model.StoreType

class CreateListUseCase(
    private val repository: FirestoreShoppingRepository
) {

    suspend operator fun invoke(
        name: String,
        storeTypes: List<StoreType>,
        isCustom: Boolean
    ): String {

        return repository.createList(
            name = name,
            storeTypes = storeTypes,
            isCustom = isCustom
        )

    }

}