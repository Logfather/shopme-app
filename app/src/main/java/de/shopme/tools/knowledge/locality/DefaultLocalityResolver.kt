package de.shopme.tools.knowledge.locality

class DefaultLocalityResolver(

    private val knowledge:

    LocalityKnowledge

) : LocalityResolver {

    override fun resolve(

        foodReference: String?

    ): Locality? {

        val reference =

            foodReference ?: return null

        return knowledge.entries[

            reference.lowercase()

        ]

    }

}