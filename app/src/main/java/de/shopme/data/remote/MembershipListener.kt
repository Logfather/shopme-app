package de.shopme.data.remote

import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import de.shopme.data.sync.SyncCoordinator

class MembershipListener(
    private val firestore: FirebaseFirestore,
    private val syncCoordinator: SyncCoordinator
) {

    private var registration: ListenerRegistration? = null
    private var isStarted = false
    private var isInitialSnapshot = true   // 🔥 CRITICAL

    fun start(userId: String) {

        if (isStarted) {
            Log.d("MEMBERSHIP", "Already started → skip")
            return
        }

        Log.d("MEMBERSHIP", "Start listening for user: $userId")

        isStarted = true

        registration = firestore
            .collection("lists")
            .whereArrayContains("sharedWith", userId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e("MEMBERSHIP", "Listener error", error)
                    return@addSnapshotListener
                }

                if (snapshot == null) return@addSnapshotListener

                snapshot.documentChanges.forEach { change ->

                    val listId = change.document.id

                    when (change.type) {

                        DocumentChange.Type.ADDED -> {
                            Log.d("MEMBERSHIP", "ADDED → start sync: $listId")
                            syncCoordinator.startSingleListSync(listId)
                        }

                        DocumentChange.Type.REMOVED -> {
                            Log.d("MEMBERSHIP", "REMOVED → stop sync: $listId")
                            syncCoordinator.stopSingleListSync(listId)
                            syncCoordinator.deleteLocalListAsync(listId)
                        }

                        DocumentChange.Type.MODIFIED -> {
                            Log.d("MEMBERSHIP", "MODIFIED: $listId")
                        }
                    }
                }
            }
    }

    fun stop() {

        if (!isStarted) return

        Log.d("MEMBERSHIP", "Stopping listener")

        registration?.remove()
        registration = null
        isStarted = false
    }
}