package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import de.shopme.tools.knowledge.gap.CatalogKnowledgeGapAnalyzer
import de.shopme.tools.knowledge.off.OFFHivraExtract
import de.shopme.tools.knowledge.off.OFFKnowledgeCandidate
import de.shopme.tools.knowledge.off.OFFKnowledgeCandidateBuilder
import de.shopme.tools.knowledge.off.OFFKnowledgeCandidateRanker
import de.shopme.tools.knowledge.reader.ResourceCatalogReader
import org.junit.Test
import java.io.File

class OFFKnowledgeCandidateExportTest {

    private val gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

    @Test
    fun exportKnowledgeCandidates() {

        val extractFile =
            File(
                "data/generated/off/off_hivra_extract.jsonl"
            )

        val outputFile =
            File(
                "data/generated/off/off_knowledge_candidates.json"
            )

        require(extractFile.exists()) {
            "OFF Hivra extract not found: ${extractFile.absolutePath}"
        }

        val catalog =
            ResourceCatalogReader()
                .read()

        val gaps =
            CatalogKnowledgeGapAnalyzer()
                .analyze(catalog)

        val builder =
            OFFKnowledgeCandidateBuilder()

        val ranker =
            OFFKnowledgeCandidateRanker()

        val rawCandidates =
            mutableListOf<OFFKnowledgeCandidate>()

        var extracts = 0

        extractFile
            .bufferedReader()
            .useLines { lines ->

                lines.forEach { line ->

                    if (line.isBlank()) {
                        return@forEach
                    }

                    val extract =
                        parseExtract(line)
                            ?: return@forEach

                    extracts++

                    val candidate =
                        builder
                            .build(
                                gaps = gaps,
                                extracts = listOf(extract)
                            )
                            .firstOrNull()
                            ?: return@forEach

                    rawCandidates += candidate

                    if (extracts % 100_000 == 0) {
                        println(
                            "Extracts scanned=$extracts rawCandidates=${rawCandidates.size}"
                        )
                    }
                }
            }

        val rankedCandidates =
            ranker.rank(rawCandidates)

        outputFile.parentFile?.mkdirs()

        outputFile.writeText(
            gson.toJson(rankedCandidates)
        )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OPEN FOOD FACTS KNOWLEDGE CANDIDATES")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Catalog items  : ${catalog.size}")
        println("Gaps           : ${gaps.size}")
        println("Extracts       : $extracts")
        println("Raw candidates : ${rawCandidates.size}")
        println("Ranked         : ${rankedCandidates.size}")
        println("Output         : ${outputFile.absolutePath}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    private fun parseExtract(
        line: String
    ): OFFHivraExtract? {

        return runCatching {
            gson.fromJson(
                JsonParser.parseString(line),
                OFFHivraExtract::class.java
            )
        }.getOrNull()
    }
}