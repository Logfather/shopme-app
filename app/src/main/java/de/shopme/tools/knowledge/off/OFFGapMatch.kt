package de.shopme.tools.knowledge.off

import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId

data class OFFGapMatch(

    val catalogFood: String,

    val offProductName: String,

    val offId: String?,

    val matchedDimensions: Set<KnowledgeDimensionId>
)