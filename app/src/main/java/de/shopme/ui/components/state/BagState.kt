package de.shopme.ui.components.state

enum class BagState {
    EMPTY,
    ACTIVE,
    DONE
}

fun resolveBagState(
    total: Int,
    checked: Int
): BagState {
    return when {
        total == 0 -> BagState.EMPTY
        checked == total -> BagState.DONE
        else -> BagState.ACTIVE
    }
}