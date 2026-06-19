package de.shopme.tools.knowledge.recipe

class DefaultRecipeResolver(

    private val knowledge: RecipeKnowledge

) : RecipeResolver {

    override fun resolve(
        foodReference: String?
    ): List<String> {

        val reference = foodReference ?: return emptyList()

        return knowledge.entries[
            reference
        ] ?: emptyList()
    }

}