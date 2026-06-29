package de.shopme.presentation.developer.report

data class KnowledgeBaselineReport(

    val catalogItems: Int,

    val totalGaps: Int,

    val matchedFoods: Int,

    val unmatchedFoods: Int,

    val dimensionCoverage: Map<String, Int>
)