package de.shopme.tools.knowledge.diet

class DefaultDietResolver(

    private val knowledge: DietKnowledge

) : DietResolver {

    override fun resolve(
        foodReference: String?
    ): Set<DietClassification> {

        val reference =
            foodReference ?: return emptySet()

        return knowledge.entries[
            reference.lowercase()
        ] ?: emptySet()

    }

}