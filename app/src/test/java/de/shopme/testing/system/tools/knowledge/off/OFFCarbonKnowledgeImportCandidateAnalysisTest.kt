package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.off.OFFCarbonKnowledgeImportCandidateFilter
import de.shopme.tools.knowledge.off.OFFKnowledgeImportCandidate
import org.junit.Test
import java.io.File

class OFFCarbonKnowledgeImportCandidateAnalysisTest {

    private val gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

    @Test
    fun analyzeCarbonImportCandidates() {

        val input =
            File(
                "data/generated/off/off_knowledge_import_candidates.json"
            )

        require(input.exists()) {
            "OFF import candidates not found: ${input.absolutePath}"
        }

        val candidates =
            gson.fromJson<List<OFFKnowledgeImportCandidate>>(
                input.readText(),
                object : TypeToken<List<OFFKnowledgeImportCandidate>>() {}.type
            )

        val carbonCandidates =
            OFFCarbonKnowledgeImportCandidateFilter()
                .filter(
                    candidates = candidates
                )

        val catalogCounts =
            mutableMapOf<String, Int>()

        carbonCandidates.forEach { candidate ->

            catalogCounts[
                candidate.catalogNormalizedName
            ] =
                (catalogCounts[
                    candidate.catalogNormalizedName
                ] ?: 0) + 1
        }

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OFF CARBON IMPORT CANDIDATE ANALYSIS")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Import candidates : ${candidates.size}")
        println("Carbon candidates : ${carbonCandidates.size}")

        println()
        println("Top Catalog Items:")
        printTopMap(
            values = catalogCounts,
            limit = 50
        )

        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    private fun printTopMap(
        values: Map<String, Int>,
        limit: Int = 50
    ) {

        if (values.isEmpty()) {
            println("- none")
            return
        }

        values
            .entries
            .sortedWith(
                compareByDescending<Map.Entry<String, Int>> {
                    it.value
                }.thenBy {
                    it.key
                }
            )
            .take(limit)
            .forEach { entry ->

                println(
                    "- ${entry.key}: ${entry.value}"
                )
            }
    }
}