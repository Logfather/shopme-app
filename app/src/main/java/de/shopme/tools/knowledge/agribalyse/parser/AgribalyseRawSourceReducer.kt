package de.shopme.tools.knowledge.agribalyse.parser

import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable
import org.apache.poi.xssf.eventusermodel.XSSFReader
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler
import org.apache.poi.xssf.usermodel.XSSFComment
import org.xml.sax.InputSource
import org.xml.sax.helpers.XMLReaderFactory
import java.io.File

class AgribalyseRawSourceReducer {

    fun reduce(
        input: File,
        output: File,
        sheetName: String = "Synthese",
        maxRows: Int? = null
    ) {
        if (!input.exists()) {
            error("Agribalyse input missing: ${input.absolutePath}")
        }

        output.parentFile?.mkdirs()

        output.bufferedWriter().use { writer ->

            OPCPackage.open(input).use { pkg ->

                val reader =
                    XSSFReader(pkg)

                val sharedStrings =
                    ReadOnlySharedStringsTable(pkg)

                val sheetIterator =
                    reader.sheetsData as XSSFReader.SheetIterator

                var foundSheet = false
                var writtenRows = 0

                while (sheetIterator.hasNext()) {
                    val stream =
                        sheetIterator.next()

                    val currentSheetName =
                        sheetIterator.sheetName

                    if (currentSheetName != sheetName) {
                        stream.close()
                        continue
                    }

                    foundSheet = true

                    stream.use { sheetStream ->

                        val handler =
                            AgribalyseSlimTsvHandler(
                                maxRows = maxRows,
                                onRow = { row ->

                                    if (maxRows == null || writtenRows < maxRows) {
                                        writer.appendLine(
                                            row.joinToString("\t") { value ->
                                                value.cleanForTsv()
                                            }
                                        )
                                        writtenRows++
                                    }
                                }
                            )

                        val xmlReader =
                            XMLReaderFactory.createXMLReader()

                        xmlReader.contentHandler =
                            XSSFSheetXMLHandler(
                                reader.stylesTable,
                                sharedStrings,
                                handler,
                                DataFormatter(),
                                false
                            )

                        xmlReader.parse(InputSource(sheetStream))
                    }

                    break
                }

                if (!foundSheet) {
                    error("Agribalyse sheet '$sheetName' not found")
                }
            }
        }
    }

    private fun String.cleanForTsv(): String {
        return replace("\t", " ")
            .replace("\n", " ")
            .replace("\r", " ")
            .trim()
    }
}

private class AgribalyseSlimTsvHandler(
    private val maxRows: Int?,
    private val onRow: (List<String>) -> Unit
) : XSSFSheetXMLHandler.SheetContentsHandler {

    private val keepColumns =
        listOf(
            "Code AGB",
            "Code CIQUAL",
            "Groupe d'aliment",
            "Sous-groupe d'aliment",
            "Nom du Produit en Français",
            "LCI Name",
            "DQR - Note de qualité de la donnée (1 excellente ; 5 très faible)",
            "mPt/kg de produit",
            "kg CO2 eq/kg de produit",
            "Pt/kg de produit",
            "m3 depriv./kg de produit"
        )

    private val keepColumnKeys =
        keepColumns
            .map { value ->
                value.normalizedHeader()
            }
            .toSet()

    private val currentRow =
        mutableMapOf<Int, String>()

    private var emittedRows =
        0

    private var headerFound =
        false

    private var selectedColumns =
        emptyList<Int>()

    private val outputHeaders =
        listOf(
            "code_agb",
            "code_ciqual",
            "food_group",
            "food_sub_group",
            "name_fr",
            "name_en",
            "data_quality_score",
            "environment_score_mpt_per_kg",
            "climate_total_kg_co2_eq_per_kg",
            "land_use_pt_per_kg",
            "water_deprivation_m3_per_kg",
            "climate_biogenic_kg_co2_eq_per_kg",
            "climate_fossil_kg_co2_eq_per_kg",
            "climate_land_use_change_kg_co2_eq_per_kg"
        )

    override fun startRow(rowNum: Int) {
        currentRow.clear()
    }

    override fun endRow(rowNum: Int) {

        if (!headerFound) {

            val values =
                currentRowValues()

            val normalizedValues =
                values.map { value ->
                    value.normalizedHeader()
                }

            if ("code agb" in normalizedValues) {

                selectedColumns =
                    values.mapIndexedNotNull { index, value ->
                        if (value.normalizedHeader() in keepColumnKeys) {
                            index
                        } else {
                            null
                        }
                    }

                if (selectedColumns.isEmpty()) {
                    error("Agribalyse header found, but no selected columns matched.")
                }

                if (selectedColumns.size != outputHeaders.size) {
                    error(
                        "Agribalyse selected column count mismatch. " +
                                "selected=${selectedColumns.size}, headers=${outputHeaders.size}"
                    )
                }

                onRow(outputHeaders)

                headerFound = true
                emittedRows++

                println("Agribalyse selected columns=${selectedColumns.size}")
            }

            return
        }

        if (maxRows != null && emittedRows >= maxRows) {
            return
        }

        val values =
            currentRowValues()

        if (values.isEmpty()) {
            return
        }

        val reduced =
            selectedColumns.map { index ->
                values.getOrNull(index).orEmpty()
            }

        onRow(reduced)

        emittedRows++
    }

    override fun cell(
        cellReference: String,
        formattedValue: String?,
        comment: XSSFComment?
    ) {
        val columnIndex =
            cellReference.toColumnIndex()

        currentRow[columnIndex] =
            formattedValue.orEmpty()
    }

    override fun headerFooter(
        text: String?,
        isHeader: Boolean,
        tagName: String?
    ) = Unit

    private fun currentRowValues(): List<String> {

        val maxColumn =
            currentRow.keys.maxOrNull()
                ?: return emptyList()

        return (0..maxColumn).map { index ->
            currentRow[index].orEmpty()
        }
    }
}

private fun String.normalizedHeader(): String {
    return trim()
        .lowercase()
        .replace(Regex("\\s+"), " ")
}

private fun String.toColumnIndex(): Int {
    val letters =
        takeWhile { char ->
            char.isLetter()
        }

    var result = 0

    letters.forEach { char ->
        result *= 26
        result += char.uppercaseChar() - 'A' + 1
    }

    return result - 1
}