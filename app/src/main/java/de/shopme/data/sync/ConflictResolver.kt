package de.shopme.data.sync

import de.shopme.domain.model.ShoppingItemEntity

enum class ConflictStrategy {
    USE_LOCAL,
    USE_REMOTE,
    MERGE
}

data class ConflictResult(
    val strategy: ConflictStrategy,
    val resolvedItem: ShoppingItemEntity? = null
)

class ConflictResolver {

    fun resolveItemConflict(
        local: ShoppingItemEntity,
        remote: ShoppingItemEntity,
        baseVersion: Long
    ): ConflictResult {

        // ============================================================
        // 🔥 STRATEGIE 1: DELETE gewinnt IMMER
        // ============================================================

        if (remote.deletedAt != null) {
            return ConflictResult(
                strategy = ConflictStrategy.USE_REMOTE,
                resolvedItem = remote
            )
        }

        if (local.deletedAt != null) {
            return ConflictResult(
                strategy = ConflictStrategy.USE_LOCAL
            )
        }

        // ============================================================
        // 🔥 STRATEGIE 2: NEWEST WINS (DEFAULT)
        // ============================================================

        return if (remote.updatedAt > local.updatedAt) {

            ConflictResult(
                strategy = ConflictStrategy.USE_REMOTE,
                resolvedItem = remote
            )

        } else {

            ConflictResult(
                strategy = ConflictStrategy.USE_LOCAL
            )
        }
    }
}