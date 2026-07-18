package de.shopme.tools.knowledge.ai.builder.allergen

import de.shopme.tools.knowledge.allergen.Allergen
import de.shopme.tools.knowledge.allergen.AllergenKnowledge
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType

class MergedCandidateAllergenKnowledgeBuilder {

    fun build(
        candidates: List<CanonicalKnowledgeCandidate>
    ) =
        build(
            candidates.asSequence()
        )

    fun build(
        candidates: Sequence<CanonicalKnowledgeCandidate>
    ): AllergenKnowledge {

        val entries =
            candidates
                .mapNotNull { candidate ->

                    val key =
                        candidate.canonicalId.trim()

                    if (key.isBlank()) {
                        return@mapNotNull null
                    }

                    val payload =
                        candidate.dimensions
                            .firstOrNull {
                                it.dimension ==
                                        KnowledgeDimensionCandidateType.ALLERGENS
                            }
                            ?.payload
                            ?: return@mapNotNull null

                    val allergens =
                        payload.toAllergens()

                    if (allergens.isEmpty()) {
                        return@mapNotNull null
                    }

                    key to allergens
                }
                .toMap()
                .toSortedMap()

        return AllergenKnowledge(
            entries = entries
        )
    }


    private fun Any.toAllergens(): Set<Allergen> {

        if (this !is Map<*, *>) {
            return emptySet()
        }

        val values =
            this["allergens"] as? List<*>
                ?: return emptySet()

        return values
            .mapNotNull { value ->

                val name =
                    (value as? String)
                        ?.normalizeAllergen()
                        ?: return@mapNotNull null

                Allergen.entries
                    .firstOrNull {
                        it.name.lowercase() == name
                    }
            }
            .toSortedSet()
    }


    private fun String.normalizeAllergen(): String =
        removePrefix("en:")
            .trim()
            .lowercase()
            .replace("-", "_")
}