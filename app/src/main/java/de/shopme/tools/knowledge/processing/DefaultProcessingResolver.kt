package de.shopme.tools.knowledge.processing

class DefaultProcessingResolver(

    private val knowledge:

    ProcessingKnowledge

) : ProcessingResolver {

    override fun resolve(

        foodReference: String?

    ): ProcessingLevel? {

        val reference =

            foodReference ?: return null

        return knowledge.entries[

            reference.lowercase()

        ]

    }

}