package de.shopme.tools.knowledge.analyzer

import de.shopme.domain.food.FoodKnowledgeEntry

class KnowledgeAnalyzer {

    fun analyze(
        entries: List<FoodKnowledgeEntry>
    ): KnowledgeStatistics {

        val distinctNormalized =
            entries
                .map { it.normalizedName }
                .distinct()
                .count()

        val distinctNutritionReferences =
            entries
                .mapNotNull { it.nutritionReference }
                .distinct()
                .count()

        val missingNutritionReferences =
            entries
                .count { it.nutritionReference == null }

        return KnowledgeStatistics(

            totalCatalogItems = entries.size,

            distinctNormalized = distinctNormalized,

            distinctNutritionReferences =
                distinctNutritionReferences,

            missingNutritionReferences =
                missingNutritionReferences

        )

    }

}