package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.off.CarbonVariantCoverageCandidate
import org.junit.Test
import java.io.File

class CarbonVariantCoverageCandidateAnalysisTest {

    private val gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

    @Test
    fun analyzeCarbonVariantCoverageCandidates() {

        val input =
            File(
                "data/generated/off/carbon_variant_coverage_candidates.json"
            )

        require(input.exists()) {
            "Carbon variant coverage candidates not found: ${input.absolutePath}"
        }

        val type =
            object : TypeToken<List<CarbonVariantCoverageCandidate>>() {}.type

        val candidates: List<CarbonVariantCoverageCandidate> =
            gson.fromJson(
                input.readText(),
                type
            )

        val resolvedCounts =
            mutableMapOf<String, Int>()

        candidates.forEach { candidate ->

            resolvedCounts[
                candidate.resolvedCarbonReference
            ] =
                (resolvedCounts[
                    candidate.resolvedCarbonReference
                ] ?: 0) + 1
        }

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 CARBON VARIANT COVERAGE ANALYSIS")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Variant candidates : ${candidates.size}")

        println()
        println("Top Base Carbon References:")

        printTopMap(
            values = resolvedCounts,
            limit = 100
        )

        println()
        println("First 100 Variant Candidates:")

        candidates
            .take(100)
            .forEach { candidate ->

                println(
                    "- ${candidate.catalogNormalizedName} -> ${candidate.resolvedCarbonReference}"
                )
            }

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