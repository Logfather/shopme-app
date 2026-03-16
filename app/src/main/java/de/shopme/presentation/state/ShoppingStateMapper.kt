package de.shopme.presentation.state

import de.shopme.domain.model.ShoppingItem
import de.shopme.domain.model.ShoppingList

fun ShoppingState.toViewState(): ShoppingViewState {

    val activeList = lists.find { it.id == activeListId }

    // Items nach Kategorie gruppieren
    val groupedItems: Map<String, List<ShoppingItem>> =
        items.groupBy { it.category ?: "Other" }

    return ShoppingViewState(
        lists = lists,
        activeList = activeList,
        groupedItems = groupedItems
    )
}