package de.shopme.domain.usecase

import de.shopme.data.datasource.firestore.FirestoreDataSource
import de.shopme.data.repository.RoomShoppingRepository
import de.shopme.domain.model.ListDeleteSnapshot

class DeleteListUseCase(
    private val roomRepository: RoomShoppingRepository,
    private val firestore: FirestoreDataSource
) {

    suspend operator fun invoke(listId: String): ListDeleteSnapshot {

        // 1. Snapshot erstellen
        val snapshot = roomRepository.createListDeleteSnapshot(listId)

        // 2. Delete ausführen
        roomRepository.deleteList(listId)

        // 3. Snapshot zurückgeben
        return snapshot
    }
}