package de.shopme.tools.knowledge.pollinator

class DefaultPollinatorResolver(

    private val knowledge: PollinatorKnowledge

) : PollinatorResolver {

    override fun resolve(
        foodReference: String?
    ): PollinatorScore? {

        val reference =
            foodReference ?: return null

        return knowledge.entries[
            reference.lowercase()
        ]

    }

}