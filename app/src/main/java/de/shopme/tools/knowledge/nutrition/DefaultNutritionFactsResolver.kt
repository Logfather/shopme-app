package de.shopme.tools.knowledge.nutrition

import de.shopme.data.sync.logging.RuntimeLog

class DefaultNutritionFactsResolver(

    private val knowledge: NutritionFactsKnowledge

) : NutritionFactsResolver {

    override fun resolve(
        foodReference: String?
    ): NutritionFacts? {

        val reference = foodReference ?: return null

        val result = knowledge.entries[reference]

        if (result == null) {

            RuntimeLog.runtime(
                "MISS ref=$reference entries=${knowledge.entries.size} containsBanane=${knowledge.entries.containsKey("banane")}"
            )

        }

        return result

    }

}