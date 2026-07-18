package de.shopme.tools.knowledge.agribalyse.extractor

import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable
import org.apache.poi.xssf.eventusermodel.XSSFReader
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler
import org.apache.poi.xssf.usermodel.XSSFComment
import org.xml.sax.InputSource
import org.xml.sax.helpers.XMLReaderFactory
import java.io.File

class AgribalyseNutritionCandidateExtractor {

    fun extract(
        file: File,
        maxCandidates: Int? = null
    ): List<CanonicalKnowledgeCandidate> {

        if (!file.exists()) {
            error("Agribalyse file does not exist: ${file.absolutePath}")
        }

        val candidates =
            mutableListOf<CanonicalKnowledgeCandidate>()

        OPCPackage.open(file).use { pkg ->

            val reader =
                XSSFReader(pkg)

            val styles =
                reader.stylesTable

            val sharedStrings =
                ReadOnlySharedStringsTable(pkg)

            val sheetIterator =
                reader.sheetsData as XSSFReader.SheetIterator

            if (!sheetIterator.hasNext()) {
                error("Agribalyse workbook has no sheets")
            }

            while (sheetIterator.hasNext()) {
                val sheetStream =
                    sheetIterator.next()

                val sheetName =
                    sheetIterator.sheetName

                println("Agribalyse sheet=$sheetName")

                sheetStream.use { stream ->
                    val handler =
                        AgribalyseSheetHandler(
                            maxCandidates = maxCandidates,
                            onCandidate = { candidate ->
                                candidates += candidate
                            }
                        )

                    val xmlReader =
                        XMLReaderFactory.createXMLReader()

                    xmlReader.contentHandler =
                        XSSFSheetXMLHandler(
                            styles,
                            sharedStrings,
                            handler,
                            DataFormatter(),
                            false
                        )

                    xmlReader.parse(InputSource(stream))
                }

                if (maxCandidates != null && candidates.size >= maxCandidates) {
                    break
                }
            }
        }

        return candidates
    }
}

private class AgribalyseSheetHandler(
    private val maxCandidates: Int?,
    private val onCandidate: (CanonicalKnowledgeCandidate) -> Unit
) : XSSFSheetXMLHandler.SheetContentsHandler {

    private val currentRowValues =
        mutableMapOf<Int, String>()

    private var rowIndex: Int = -1

    private var header: Map<String, Int> = emptyMap()

    private var nameColumn: Int? = null
    private var energyColumn: Int? = null

    private var emitted = 0

    private var headerResolved = false

    override fun startRow(rowNum: Int) {
        rowIndex = rowNum
        currentRowValues.clear()
    }

    override fun endRow(rowNum: Int) {

        if (!headerResolved && rowNum < 20 && currentRowValues.isNotEmpty()) {
            println("row=$rowNum values=${currentRowValues.values.joinToString(" | ")}")
        }

        if (!headerResolved) {
            val candidateHeader =
                currentRowValues.entries.associate { entry ->
                    entry.value.trim() to entry.key
                }

            val candidateNameColumn =
                findColumnOrNull(
                    header = candidateHeader,
                    candidates = listOf(
                        "Nom du Produit en Français",
                        "Nom du produit en Français",
                        "Nom du produit",
                        "alim_nom_fr"
                    )
                )

            val candidateEnergyColumn =
                findColumnOrNull(
                    header = candidateHeader,
                    candidates = listOf(
                        "Energie, Règlement UE N° 1169/2011 (kcal/100 g)",
                        "Energie (kcal/100 g)",
                        "Energie, Règlement UE N° 1169/2011 (kcal/100g)",
                        "energie_kcal_100g"
                    )
                )

            if (candidateNameColumn != null && candidateEnergyColumn != null) {
                header = candidateHeader
                nameColumn = candidateNameColumn
                energyColumn = candidateEnergyColumn
                headerResolved = true

                println("Agribalyse header found at row=$rowNum")
                println("nameColumn=$nameColumn")
                println("energyColumn=$energyColumn")
            }

            return
        }

        if (maxCandidates != null && emitted >= maxCandidates) {
            return
        }

        val resolvedNameColumn =
            nameColumn ?: return

        val resolvedEnergyColumn =
            energyColumn ?: return

        val name =
            currentRowValues[resolvedNameColumn]
                ?.trim()
                ?.lowercase()
                ?.takeIf { it.isNotBlank() }
                ?: return

        val energyKcal =
            currentRowValues[resolvedEnergyColumn]
                ?.trim()
                ?.replace(",", ".")
                ?.toDoubleOrNull()
                ?: return

        onCandidate(
            CanonicalKnowledgeCandidate(
                canonicalId = name,
                aliases = setOf(name),
                dimensions = listOf(
                    KnowledgeDimensionCandidate(
                        dimension = KnowledgeDimensionCandidateType.NUTRITION,
                        payload = mapOf(
                            "energyKcalPer100g" to energyKcal
                        )
                    )
                ),
                metadata = CandidateMetadata(
                    source = "agribalyse",
                    sourceId = name,
                    confidence = 1.0,
                    version = "3.2",
                    attributes = mapOf(
                        "foodName" to name
                    )
                )
            )
        )

        emitted++
    }

    override fun cell(
        cellReference: String,
        formattedValue: String?,
        comment: XSSFComment?
    ) {
        val columnIndex =
            cellReference.toColumnIndex()

        if (formattedValue != null) {
            currentRowValues[columnIndex] = formattedValue
        }
    }

    override fun headerFooter(
        text: String?,
        isHeader: Boolean,
        tagName: String?
    ) = Unit

    private fun findColumnOrNull(
        header: Map<String, Int>,
        candidates: List<String>
    ): Int? {
        return candidates.firstNotNullOfOrNull { candidate ->
            header[candidate]
        }
    }
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