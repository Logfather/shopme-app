package de.shopme.tools.knowledge.category

class DefaultCategoryResolver(

    private val knowledge: CategoryKnowledge

) : CategoryResolver {

    private val lookup =
        knowledge.asLookupMap()

    override fun resolve(
        category: String
    ): String? {

        return lookup[
            category.lowercase()
        ]

    }

}