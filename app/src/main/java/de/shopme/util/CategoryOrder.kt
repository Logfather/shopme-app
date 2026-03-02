package de.shopme.util

object CategoryOrder {

    private val order = listOf(
        "Obst",
        "Gemüse",
        "Backwaren",
        "Fleisch",
        "Wurst",
        "Sonstiges"
    )

    fun indexOf(category: String): Int {
        return order.indexOf(category)
            .takeIf { it >= 0 }
            ?: Int.MAX_VALUE
    }

    fun sortedCategories(categories: Set<String>): List<String> {
        return categories.sortedBy { indexOf(it) }
    }
}
