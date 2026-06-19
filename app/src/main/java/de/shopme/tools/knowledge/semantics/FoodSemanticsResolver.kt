package de.shopme.tools.knowledge.semantics

interface FoodSemanticsResolver {

    fun resolve(
        taxonomyNode: String?
    ): FoodSemanticsEntry?

}