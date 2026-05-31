package de.shopme.data.sync.queue

import de.shopme.data.sync.logging.SyncLog

object QueuePolicy {

    fun resolve(
        existing: ChangeQueueEntity?,
        incoming: ChangeQueueEntity
    ): QueueResolution {

        if (existing == null) {

            return QueueResolution.Enqueue(
                incoming
            )
        }

        val existingOperation =
            existing.operation

        val incomingOperation =
            incoming.operation

        // ============================================================
        // CREATE + UPDATE
        // ============================================================

        if (
            existingOperation == "CREATE" &&
            incomingOperation == "UPDATE"
        ) {

            SyncLog.queue(
                "[Dedup] Keep CREATE skip UPDATE id=${incoming.entityId}"
            )

            return QueueResolution.Skip
        }

        // ============================================================
        // CREATE + DELETE
        // ============================================================

        if (
            existingOperation == "CREATE" &&
            incomingOperation == "DELETE"
        ) {

            SyncLog.queue(
                "[Dedup] Drop CREATE+DELETE id=${incoming.entityId}"
            )

            return QueueResolution.DeleteExisting(
                existing
            )
        }

        return QueueResolution.Enqueue(
            incoming
        )
    }
}