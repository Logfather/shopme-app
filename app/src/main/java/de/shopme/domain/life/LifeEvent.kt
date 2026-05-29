package de.shopme.domain.life

sealed interface LifeEvent {

    val timestamp: Long

    // ------------------------------------------------------------
    // ITEM EVENTS
    // ------------------------------------------------------------

    data class ItemAdded(
        val itemId: String,
        val listId: String,
        val itemName: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : LifeEvent

    data class ItemChecked(
        val itemId: String,
        val listId: String,
        val checked: Boolean,
        override val timestamp: Long = System.currentTimeMillis()
    ) : LifeEvent

    data class ItemDeleted(
        val itemId: String,
        val listId: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : LifeEvent

    // ------------------------------------------------------------
    // LIST EVENTS
    // ------------------------------------------------------------

    data class ListCreated(
        val listId: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : LifeEvent

    data class ListShared(
        val listId: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : LifeEvent

    data class InviteAccepted(
        val listId: String,
        val userId: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : LifeEvent

    // ------------------------------------------------------------
    // SYNC EVENTS
    // ------------------------------------------------------------

    data class SyncConflictDetected(
        val entityId: String,
        val entityType: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : LifeEvent
}