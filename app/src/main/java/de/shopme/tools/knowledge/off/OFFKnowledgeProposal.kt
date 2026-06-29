package de.shopme.tools.knowledge.off

import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId

data class OFFKnowledgeProposal(

    val catalogNormalizedName: String,

    val offCode: String?,

    val offProductName: String,

    val source: String = "off",

    val proposedReferences: Map<KnowledgeDimensionId, String>,

    val dimensions: Set<KnowledgeDimensionId>
)