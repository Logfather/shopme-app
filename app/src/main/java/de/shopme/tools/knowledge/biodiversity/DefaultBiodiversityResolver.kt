package de.shopme.tools.knowledge.biodiversity

class DefaultBiodiversityResolver(

    private val knowledge:

    BiodiversityKnowledge

) : BiodiversityResolver {

    override fun resolve(

        foodReference: String?

    ): BiodiversityScore? {

        val reference =

            foodReference ?: return null

        return knowledge.entries[

            reference.lowercase()

        ]

    }

}