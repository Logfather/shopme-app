package de.shopme.tools.knowledge.production

interface ProductionResolver {

    fun resolve(
        foodReference: String?
    ): Set<ProductionMethod>

}