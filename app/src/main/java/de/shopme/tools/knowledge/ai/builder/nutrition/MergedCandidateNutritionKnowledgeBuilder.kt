package de.shopme.tools.knowledge.ai.builder.nutrition

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import de.shopme.tools.knowledge.nutrition.NutritionFacts
import de.shopme.tools.knowledge.nutrition.NutritionFactsKnowledge


class MergedCandidateNutritionKnowledgeBuilder {

    fun build(
        candidates: List<CanonicalKnowledgeCandidate>
    ) =
        build(
            candidates.asSequence()
        )

    fun build(
        candidates: Sequence<CanonicalKnowledgeCandidate>
    ): NutritionFactsKnowledge {

        val entries = candidates
            .mapNotNull { candidate ->

                val payload = candidate.dimensions
                    .firstOrNull {
                        it.dimension == KnowledgeDimensionCandidateType.NUTRITION
                    }
                    ?.payload
                    ?: return@mapNotNull null

                val nutrition = payload.toNutritionFacts()
                    ?: return@mapNotNull null

                val key =
                    candidate.canonicalId.trim()

                if (key.isBlank()) {
                    return@mapNotNull null
                }

                key to nutrition
            }
            .toMap()
            .toSortedMap()

        return NutritionFactsKnowledge(entries)
    }

    private fun Any.toNutritionFacts(): NutritionFacts? {
        if (this is NutritionFacts) {
            return this
        }

        if (this !is Map<*, *>) {
            return null
        }

        return NutritionFacts(
            calories =
                double("energyKcalPer100g"),

            fat =
                double("fatPer100g"),

            saturatedFat =
                double("saturatedFatPer100g"),

            carbohydrates =
                double("carbohydratesPer100g"),

            sugar =
                double("sugarsPer100g"),

            fiber =
                double("fiberPer100g"),

            protein =
                double("proteinsPer100g"),

            salt =
                double("saltPer100g")
        )
    }

    private fun Map<*, *>.double(
        key: String
    ): Double {
        return (this[key] as? Number)
            ?.toDouble()
            ?: 0.0
    }
}