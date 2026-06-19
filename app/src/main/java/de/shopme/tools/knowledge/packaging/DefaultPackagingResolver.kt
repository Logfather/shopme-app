package de.shopme.tools.knowledge.packaging

class DefaultPackagingResolver(

    private val knowledge:

    PackagingKnowledge

) : PackagingResolver {

    override fun resolve(
        foodReference: String?
    ): Packaging? {

        val reference =
            foodReference ?: return null

        return knowledge.entries[
            reference.lowercase()
        ]

    }

}