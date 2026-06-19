package de.shopme.tools.knowledge.production

class DefaultProductionResolver(

    private val knowledge: ProductionKnowledge

) : ProductionResolver {

    override fun resolve(
        foodReference: String?
    ): Set<ProductionMethod> {

        val reference =
            foodReference ?: return emptySet()

        return knowledge.entries[
            reference.lowercase()
        ] ?: emptySet()

    }

}