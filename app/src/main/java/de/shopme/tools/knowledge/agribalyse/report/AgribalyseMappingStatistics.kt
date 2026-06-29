package de.shopme.tools.knowledge.agribalyse.report

data class AgribalyseMappingStatistics(
    val totalRows: Int,
    val validRows: Int,
    val mappedRows: Int,
    val mappedReferences: Map<String, Int> = emptyMap(),
    val unmappedReferences: Map<String, Int> = emptyMap()
){

    val unmappedRows: Int
        get() = validRows - mappedRows
}