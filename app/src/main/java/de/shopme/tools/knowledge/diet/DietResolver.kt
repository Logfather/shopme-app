package de.shopme.tools.knowledge.diet

interface DietResolver {

    fun resolve(
        foodReference: String?
    ): Set<DietClassification>

}