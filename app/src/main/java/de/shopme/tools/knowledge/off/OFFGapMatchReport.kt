package de.shopme.tools.knowledge.off

import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId

data class OFFGapMatchReport(

    val totalGaps: Int,

    val matchedFoods: Int,

    val unmatchedFoods: Int,

    val dimensionCoverage: Map<KnowledgeDimensionId, Int>,

    val matches: List<OFFGapMatch>
)