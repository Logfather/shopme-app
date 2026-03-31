package de.shopme.data.sync

import de.shopme.domain.model.ShoppingItemEntity

class ConflictResolver {

    fun shouldApplyRemote(
        local: ShoppingItemEntity?,
        remote: ShoppingItemEntity
    ): Boolean {

        if (local == null) return true

        return when {

            // 🔥 DELETE hat höchste Priorität
            remote.deletedAt != null -> true

            local.deletedAt != null -> false

            // Version = updatedAt
            remote.updatedAt > local.updatedAt -> true

            remote.updatedAt == local.updatedAt -> false

            else -> false
        }
    }
}