package de.shopme.tools.knowledge.nutrition

import de.shopme.data.sync.logging.RuntimeLog

class DefaultNutritionAliasResolver(

    private val aliases: Map<String, String>

) : NutritionAliasResolver {

    override fun resolve(
        foodReference: String?
    ): String? {

        val reference =
            foodReference ?: return null

        return aliases[
            reference.lowercase()
        ]

    }

}