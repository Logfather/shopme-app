package de.shopme.domain.usecase

import de.shopme.data.repository.RoomShoppingRepository
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.domain.model.StoreType

class CreateListUseCase(
    private val roomRepository: RoomShoppingRepository
) {

    suspend operator fun invoke(
        name: String,
        storeTypes: List<StoreType>,
        isCustom: Boolean
    ): String {

            val listId = java.util.UUID.randomUUID().toString()

            val now = System.currentTimeMillis()

            val list = ShoppingListEntity(
                id = listId,
                name = name,
                ownerId = "", // später optional setzen
                storeTypes = storeTypes,
                itemCount = 0,
                createdAt = now,
                updatedAt = now
            )

            roomRepository.upsertLists(listOf(list))

            // optional: ChangeQueue (kann später erweitert werden)

            return listId

    }

}