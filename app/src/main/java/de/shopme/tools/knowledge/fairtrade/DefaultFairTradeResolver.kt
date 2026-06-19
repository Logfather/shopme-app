package de.shopme.tools.knowledge.fairtrade

class DefaultFairTradeResolver(

    private val knowledge: FairTradeKnowledge

) : FairTradeResolver {

    override fun resolve(

        foodReference: String?

    ): FairTrade? {

        val reference =

            foodReference ?: return null

        return knowledge.entries[

            reference.lowercase()

        ]

    }

}