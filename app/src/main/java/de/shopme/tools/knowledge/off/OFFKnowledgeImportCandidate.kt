package de.shopme.tools.knowledge.off

import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId

data class OFFKnowledgeImportCandidate(

    val catalogNormalizedName: String,

    val dimension: KnowledgeDimensionId,

    val source: String,

    val reference: String,

    val offCode: String?,

    val offProductName: String
)