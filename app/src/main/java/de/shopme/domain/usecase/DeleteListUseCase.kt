package de.shopme.domain.usecase

import de.shopme.data.datasource.firestore.FirestoreDataSource
import de.shopme.data.repository.RoomShoppingRepository

class DeleteListUseCase(
    private val roomRepository: RoomShoppingRepository,
    private val firestore: FirestoreDataSource
) {

    suspend operator fun invoke(listId: String) {

        // 1️⃣ Lokal löschen
        roomRepository.deleteList(listId)

        // 🔥 2️⃣ Remote löschen (FEHLT BISHER)
        try {
            firestore.deleteList(listId)
        } catch (e: Exception) {
            // optional später → ChangeQueue
        }
    }
}