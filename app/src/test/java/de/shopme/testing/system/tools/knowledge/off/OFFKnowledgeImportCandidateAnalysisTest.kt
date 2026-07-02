package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.off.OFFKnowledgeImportCandidate
import org.junit.Test
import java.io.File

class OFFKnowledgeImportCandidateAnalysisTest {

    private val gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

    @Test
    fun analyzeImportCandidates() {

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

        val dimensionCounts =
            mutableMapOf<String, Int>()

        val sourceCounts =
            mutableMapOf<String, Int>()

        val catalogCounts =
            mutableMapOf<String, Int>()

        candidates.forEach { candidate ->

            dimensionCounts[
                candidate.dimension.name
            ] =
                (dimensionCounts[
                    candidate.dimension.name
                ] ?: 0) + 1

            sourceCounts[
                candidate.source
            ] =
                (sourceCounts[
                    candidate.source
                ] ?: 0) + 1

            catalogCounts[
                candidate.catalogNormalizedName
            ] =
                (catalogCounts[
                    candidate.catalogNormalizedName
                ] ?: 0) + 1
        }

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OFF IMPORT CANDIDATE ANALYSIS")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Candidates : ${candidates.size}")

        println()
        println("Dimensions:")
        printTopMap(
            dimensionCounts
        )

        println()
        println("Sources:")
        printTopMap(
            sourceCounts
        )

        println()
        println("Top Catalog Items:")
        printTopMap(
            catalogCounts,
            limit = 50
        )

        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    private fun printTopMap(
        values: Map<String, Int>,
        limit: Int = 100
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