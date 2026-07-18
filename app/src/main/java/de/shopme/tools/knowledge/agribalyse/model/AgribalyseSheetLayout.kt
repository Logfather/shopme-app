package de.shopme.tools.knowledge.agribalyse.model

data class AgribalyseSheetLayout(
    val sheetType: AgribalyseSheetType,
    val descriptionRowIndex: Int,
    val headerRowIndex: Int,
    val firstDataRowIndex: Int
) {

    companion object {

        val synthesis = AgribalyseSheetLayout(
            sheetType = AgribalyseSheetType.SYNTHESIS,
            descriptionRowIndex = 0,
            headerRowIndex = 2,
            firstDataRowIndex = 3
        )
    }
}