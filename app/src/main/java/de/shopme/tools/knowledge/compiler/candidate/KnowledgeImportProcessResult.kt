package de.shopme.tools.knowledge.compiler.candidate

import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate

data class KnowledgeImportProcessResult(
    val isSuccess: Boolean,
    val errors: List<String>,
    val acceptedCandidates: List<CanonicalKnowledgeCandidate>,
    val catalogItems: List<CatalogItem>
)