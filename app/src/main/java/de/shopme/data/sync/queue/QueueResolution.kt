package de.shopme.data.sync.queue

sealed class QueueResolution {

    data object Skip : QueueResolution()

    data class Enqueue(
        val entity: ChangeQueueEntity
    ) : QueueResolution()

    data class DeleteExisting(
        val entity: ChangeQueueEntity
    ) : QueueResolution()
}