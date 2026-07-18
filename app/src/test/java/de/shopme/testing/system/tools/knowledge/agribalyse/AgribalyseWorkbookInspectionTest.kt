package de.shopme.testing.system.tools.knowledge.agribalyse

import de.shopme.tools.knowledge.agribalyse.adapter.AgribalyseAIImportAdapter
import de.shopme.tools.knowledge.agribalyse.loader.AgribalyseExcelReader
import de.shopme.tools.knowledge.agribalyse.mapper.AgribalyseCanonicalCandidateBuilder
import de.shopme.tools.knowledge.agribalyse.mapper.AgribalyseRawProductMapper
import de.shopme.tools.knowledge.agribalyse.model.AgribalyseSheetLayout
import de.shopme.tools.knowledge.agribalyse.model.AgribalyseSheetType
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildRequest
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceInfo
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceType
import kotlin.test.Test

class AgribalyseWorkbookInspectionTest {

    @Test
    fun inspectWorkbook() {

        val reader = AgribalyseExcelReader()

        val file = reader.defaultAgribalyseFile()
        val sheets = reader.sheets(file)

        println()
        println("Workbook")
        println("========")
        println("File: ${file.absolutePath}")
        println()

        println("Sheets (${sheets.size})")
        println()

        sheets.forEach {

            println(
                "%2d. %-35s %s".format(
                    it.index + 1,
                    it.name,
                    it.type
                )
            )
        }

        val previewRows = reader.previewRows(
            sheetType = AgribalyseSheetType.SYNTHESIS,
            maxRows = 10
        )

        println()
        println("SYNTHESIS preview")
        println("=================")
        println()

        previewRows.forEachIndexed { index, row ->
            println("Row ${index + 1}")
            row.forEachIndexed { cellIndex, value ->
                println("  [$cellIndex] $value")
            }
            println()
        }

        val header = reader.readHeader(
            sheetType = AgribalyseSheetType.SYNTHESIS,
            layout = AgribalyseSheetLayout.synthesis
        )

        println()
        println("SYNTHESIS HEADER")
        println("================")
        println()

        header.forEachIndexed { index, value ->
            println("[$index] $value")
        }

        val records = reader.readRecords(
            sheetType = AgribalyseSheetType.SYNTHESIS,
            layout = AgribalyseSheetLayout.synthesis,
            maxRecords = 5
        )

        println()
        println("SYNTHESIS RECORDS")
        println("=================")
        println()

        records.forEachIndexed { recordIndex, record ->

            println("Record ${recordIndex + 1}")

            record.forEach { (key, value) ->
                println("  $key = $value")
            }

            println()
        }

        val rawProducts = records.map {
            AgribalyseRawProductMapper().map(it)
        }

        println()
        println("AGRIBALYSE RAW PRODUCTS")
        println("=======================")
        println()

        rawProducts.forEachIndexed { index, product ->
            println("Raw Product ${index + 1}")
            println(product)
            println()
        }

        val inputs = rawProducts
            .map { AgribalyseAIImportAdapter().adapt(it) }

        val result = AgribalyseCanonicalCandidateBuilder().build(
            AIKnowledgeBuildRequest(
                source = AIKnowledgeSourceInfo(
                    type = AIKnowledgeSourceType.AGRIBALYSE,
                    name = "agribalyse",
                    version = "3.2"
                ),
                inputs = inputs
            )
        )

        val candidates = result.candidates

        println("Agribalyse candidates: ${candidates.size}")

        candidates.take(20).forEach { candidate ->
            println(candidate)
        }

        println()
    }
}