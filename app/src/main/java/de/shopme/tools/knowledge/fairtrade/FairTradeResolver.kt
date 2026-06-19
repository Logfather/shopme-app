package de.shopme.tools.knowledge.fairtrade

interface FairTradeResolver {

    fun resolve(
        foodReference: String?
    ): FairTrade?

}