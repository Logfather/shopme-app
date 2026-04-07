package de.shopme.domain.usecase

import de.shopme.data.repository.RoomShoppingRepository
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.domain.model.StoreType
import android.util.Log

class CreateListUseCase(
    private val roomRepository: RoomShoppingRepository
) {

    suspend operator fun invoke(
        name: String,
        storeTypes: List<StoreType>
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

        // 🔥 WICHTIG: v6-konformer Einstieg
        roomRepository.createList(list)

        return listId
    }
}