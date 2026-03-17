package de.shopme.data.sync

import de.shopme.domain.model.ShoppingItemEntity

class ConflictResolver {

    fun shouldApplyRemote(
        local: ShoppingItemEntity?,
        remote: ShoppingItemEntity
    ): Boolean {

        if (local == null) return true

        return when {

            // Version gewinnt
            remote.version > local.version -> true

            // gleiche Version → updatedAt entscheidet
            remote.version == local.version ->
                remote.updatedAt > local.updatedAt

            else -> false
        }
    }
}