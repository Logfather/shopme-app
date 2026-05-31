package de.shopme.data.remote

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import de.shopme.data.sync.SyncCoordinator
import de.shopme.data.sync.logging.RecoveryLog
import de.shopme.data.sync.logging.SyncLog

class MembershipListener(
    private val firestore: FirebaseFirestore,
    private val syncCoordinator: SyncCoordinator
) {

    private var registration: ListenerRegistration? = null
    private var isStarted = false

    fun start(userId: String) {

        if (isStarted) {
            SyncLog.guard(
                "[Membership] Already started"
            )
            return
        }

        SyncLog.lifecycle(
            "[Membership] Start listener user=$userId"
        )

        isStarted = true

        registration = firestore
            .collection("lists")
            .whereArrayContains("sharedWith", userId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    RecoveryLog.processError(
                        "Membership listener error",
                        error
                    )
                    return@addSnapshotListener
                }

                if (snapshot == null) return@addSnapshotListener

                snapshot.documentChanges.forEach { change ->

                    val listId = change.document.id

                    when (change.type) {

                        DocumentChange.Type.ADDED -> {
                            SyncLog.realtime(
                                "[Membership] Added list=$listId"
                            )
                            syncCoordinator.startSingleListSync(listId)
                        }

                        DocumentChange.Type.REMOVED -> {
                            SyncLog.realtime(
                                "[Membership] Removed list=$listId"
                            )
                            syncCoordinator.stopSingleListSync(listId)
                            syncCoordinator.deleteLocalListAsync(listId)
                        }

                        DocumentChange.Type.MODIFIED -> Unit
                    }
                }
            }
    }

    fun stop() {

        if (!isStarted) return

        SyncLog.lifecycle(
            "[Membership] Stop listener"
        )

        registration?.remove()
        registration = null
        isStarted = false
    }
}