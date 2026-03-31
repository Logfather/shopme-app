package de.shopme.domain.model

data class ShoppingItem(
    val id: String,
    val listId: String,
    val name: String,
    val quantity: Int,
    val category: String,
    val isChecked: Boolean,
    val deletedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val syncStatus: SyncStatus = SyncStatus.Synced
)