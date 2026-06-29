package de.shopme.tools.knowledge.openfoodfacts

import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId

data class OFFKnowledgeProposalApplyEntry(

    val catalogNormalizedName: String,

    val dimension: KnowledgeDimensionId,

    val reference: String,

    val source: String,

    val offCode: String?,

    val offProductName: String
)