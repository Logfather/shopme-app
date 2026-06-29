package de.shopme.tools.knowledge.carbon.report

data class CatalogCarbonGapReport(
    val catalogItems: Int,
    val carbonCovered: Int,
    val missingCarbon: Int,
    val agribalyseCandidates: List<String>
)