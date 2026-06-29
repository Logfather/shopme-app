package de.shopme.tools.knowledge.compiler.candidate

import de.shopme.domain.catalog.CatalogItem
import de.shopme.domain.catalog.model.KnowledgeReference
import de.shopme.domain.catalog.model.KnowledgeReferences
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType

class CanonicalKnowledgeCandidateMapper {

    private fun CanonicalKnowledgeCandidate.dimensionReference(
        type: KnowledgeDimensionCandidateType
    ): KnowledgeReference? {

        val dimension =
            dimensions.firstOrNull {
                it.dimension == type
            } ?: return null

        return KnowledgeReference(
            reference = canonicalId,
            source = metadata.source,
            value = dimension.payload.toString()
        )
    }

    private fun mapKnowledge(
        candidate: CanonicalKnowledgeCandidate
    ): KnowledgeReferences {

        return KnowledgeReferences(
            taxonomy = candidate.dimensionReference(
                KnowledgeDimensionCandidateType.TAXONOMY
            )
        )
    }

    fun map(
        candidate: CanonicalKnowledgeCandidate
    ): CatalogItem {

        return CatalogItem(
            itemname = candidate.canonicalId,
            category = "",
            production = "",
            normalized = candidate.canonicalId,
            plural = "",
            colloquial = candidate.aliases.toList().sorted(),
            phonetic_tokens = emptyList(),
            autocomplete_tokens = emptyList(),
            knowledge = mapKnowledge(candidate)
        )
    }
}