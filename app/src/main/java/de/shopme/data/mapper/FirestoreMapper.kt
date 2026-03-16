package de.shopme.data.mapper

import com.google.firebase.firestore.DocumentSnapshot
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.domain.model.StoreType

class FirestoreMapper {

    fun fromFirestore(doc: DocumentSnapshot): ShoppingListEntity {

        val data = doc.data ?: emptyMap()

        val storeTypes =
            (data["storeTypes"] as? List<*>)
                ?.mapNotNull { StoreType.valueOf(it as String) }
                ?: emptyList()

        return ShoppingListEntity(
            id = doc.id,
            name = data["name"] as String,
            ownerId = data["ownerId"] as String,
            storeTypes = storeTypes,
            itemCount = (data["itemCount"] as? Long ?: 0).toInt(),
            createdAt = data["createdAt"] as Long,
            updatedAt = data["updatedAt"] as Long
        )
    }
}