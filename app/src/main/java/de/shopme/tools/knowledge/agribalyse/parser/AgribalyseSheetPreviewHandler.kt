package de.shopme.tools.knowledge.agribalyse.parser

import org.apache.poi.xssf.model.SharedStrings
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

class AgribalyseSheetPreviewHandler(
    private val sharedStrings: SharedStrings,
    private val maxRows: Int,
    private val onRow: (List<String>) -> Unit
) : DefaultHandler() {

    private val currentRow = mutableListOf<String>()
    private val currentValue = StringBuilder()

    private var insideValue = false
    private var currentCellType: String? = null
    private var rowCount = 0

    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?
    ) {
        when (qName) {
            "row" -> currentRow.clear()

            "c" -> currentCellType = attributes?.getValue("t")

            "v", "t" -> {
                currentValue.clear()
                insideValue = true
            }
        }
    }

    override fun characters(
        ch: CharArray,
        start: Int,
        length: Int
    ) {
        if (insideValue) {
            currentValue.append(ch, start, length)
        }
    }

    override fun endElement(
        uri: String?,
        localName: String?,
        qName: String?
    ) {
        when (qName) {
            "v", "t" -> {
                currentRow += resolveCellValue(
                    rawValue = currentValue.toString(),
                    cellType = currentCellType
                )
                insideValue = false
            }

            "c" -> currentCellType = null

            "row" -> {
                if (rowCount < maxRows) {
                    onRow(currentRow.toList())
                    rowCount++
                }
            }
        }
    }

    private fun resolveCellValue(
        rawValue: String,
        cellType: String?
    ): String {
        return when (cellType) {
            "s" -> {
                val index = rawValue.toIntOrNull()
                if (index == null) rawValue else sharedStrings.getItemAt(index).string
            }

            else -> rawValue
        }
    }
}