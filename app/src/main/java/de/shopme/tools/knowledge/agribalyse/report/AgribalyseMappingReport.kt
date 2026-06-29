package de.shopme.tools.knowledge.agribalyse.report

data class AgribalyseMappingReport(
    val totalRows: Int,
    val mappedRows: Int,
    val unmappedRows: Int,
    val mappedReferences: Map<String, Int> = emptyMap(),
    val unmappedReferences: Map<String, Int> = emptyMap()
)