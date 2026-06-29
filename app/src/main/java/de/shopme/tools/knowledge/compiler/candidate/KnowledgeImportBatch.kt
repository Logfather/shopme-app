package de.shopme.tools.knowledge.compiler.candidate

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate

data class KnowledgeImportBatch(
    val candidates: List<CanonicalKnowledgeCandidate>,
    val metadata: KnowledgeImportBatchMetadata
)

data class KnowledgeImportBatchMetadata(
    val source: String,
    val generatedBy: String,
    val generatedAt: String,
    val promptVersion: String? = null
)