package de.shopme.data.remote

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import android.util.Log
import de.shopme.data.sync.SyncCoordinator

class MembershipListener(
    private val firestore: FirebaseFirestore,
    private val syncCoordinator: SyncCoordinator
) {

    private var registration: ListenerRegistration? = null

    fun start(userId: String) {

        Log.d("MEMBERSHIP", "Start listening for user: $userId")

        registration?.remove()

        registration = firestore
            .collection("list_members")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {

                    Log.e("MEMBERSHIP", "Listener error", error)

                    // 🔥 WICHTIG: Permission Fehler sichtbar behandeln
                    if (error.message?.contains("PERMISSION_DENIED") == true) {
                        Log.e("MEMBERSHIP", "PERMISSION_DENIED → check Firestore rules")
                    }

                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.w("MEMBERSHIP", "Snapshot is null")
                    return@addSnapshotListener
                }

                if (snapshot.isEmpty) {
                    Log.d("MEMBERSHIP", "No memberships found")
                }

                snapshot.documentChanges.forEach { change ->

                    val listId = change.document.getString("listId")

                    if (listId == null) {
                        Log.w("MEMBERSHIP", "Missing listId in document")
                        return@forEach
                    }

                    when (change.type) {

                        DocumentChange.Type.ADDED -> {
                            Log.d("MEMBERSHIP", "ADDED: $listId")
                            syncCoordinator.startSingleListSync(listId)
                        }

                        DocumentChange.Type.REMOVED -> {
                            Log.d("MEMBERSHIP", "REMOVED: $listId")
                            syncCoordinator.stopSingleListSync(listId)
                            syncCoordinator.deleteLocalListAsync(listId)
                        }

                        DocumentChange.Type.MODIFIED -> {
                            Log.d("MEMBERSHIP", "MODIFIED: $listId")
                            // aktuell keine Aktion nötig
                        }
                    }
                }
            }
    }

    fun stop() {

        if (registration != null) {
            Log.d("MEMBERSHIP", "Stopping listener")
            registration?.remove()
            registration = null
        } else {
            Log.d("MEMBERSHIP", "Stop called but no active listener")
        }
    }
}