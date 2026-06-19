package de.shopme.tools.knowledge.nutriscore

interface NutriScoreResolver {

    fun resolve(
        foodReference: String?
    ): NutriScore?

}