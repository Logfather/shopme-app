package de.shopme.data.sync

import com.google.firebase.firestore.FirebaseFirestore
import de.shopme.data.datasource.room.ListDao
import de.shopme.domain.model.ShoppingListEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirestoreSyncManager(
    private val firestore: FirebaseFirestore,
    private val listDao: ListDao
) {

    fun startSync(userId: String) {

        firestore.collection("lists")
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, _ ->

                val lists = snapshot?.documents?.mapNotNull {
                    it.toObject(ShoppingListEntity::class.java)
                } ?: emptyList()

                CoroutineScope(Dispatchers.IO).launch {

                    listDao.insertLists(lists)

                }

            }

    }

}