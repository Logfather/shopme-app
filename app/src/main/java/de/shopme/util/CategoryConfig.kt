package de.shopme.util

data class CategoryConfig(
    val categories: List<CategoryGroup> = emptyList()
)

data class CategoryGroup(
    val name: String = "",
    val products: List<String> = emptyList()
)