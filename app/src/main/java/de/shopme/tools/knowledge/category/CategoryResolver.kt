package de.shopme.tools.knowledge.category

interface CategoryResolver {

    fun resolve(
        category: String
    ): String?

}