package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.carbon.CarbonFootprint
import de.shopme.tools.knowledge.carbon.CarbonKnowledge
import de.shopme.tools.knowledge.off.CarbonVariantCoverageMergeCandidate
import org.junit.Test
import java.io.File
import java.util.TreeMap

class CarbonKnowledgeArtifactMergeTest {

    private val gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

    @Test
    fun mergeCarbonKnowledgeArtifacts() {

        val offArtifactFile =
            File(
                "data/generated/off/carbon_footprint_from_off.json"
            )

        val variantFile =
            File(
                "data/generated/off/carbon_variant_coverage_merge_candidates.json"
            )

        val outputFile =
            File(
                "data/generated/off/carbon_footprint_final.json"
            )

        require(offArtifactFile.exists()) {
            "OFF carbon artifact not found: ${offArtifactFile.absolutePath}"
        }

        require(variantFile.exists()) {
            "Variant merge candidates not found: ${variantFile.absolutePath}"
        }

        val offKnowledge =
            gson.fromJson(
                offArtifactFile.readText(),
                CarbonKnowledge::class.java
            )

        val variantCandidates =
            gson.fromJson<List<CarbonVariantCoverageMergeCandidate>>(
                variantFile.readText(),
                object :
                    TypeToken<List<CarbonVariantCoverageMergeCandidate>>() {}.type
            )

        val mergedEntries =
            TreeMap(
                offKnowledge.entries
            )

        var added = 0

        variantCandidates.forEach { candidate ->

            if (
                mergedEntries.containsKey(
                    candidate.catalogNormalizedName
                )
            ) {
                return@forEach
            }

            mergedEntries[
                candidate.catalogNormalizedName
            ] =
                CarbonFootprint(
                    kilogramsPerKilogram =
                        candidate.kilogramsPerKilogram
                )

            added++
        }

        val mergedKnowledge =
            CarbonKnowledge(
                entries = mergedEntries
            )

        outputFile.parentFile?.mkdirs()

        outputFile.writeText(
            gson.toJson(
                mergedKnowledge
            )
        )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 CARBON KNOWLEDGE MERGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Direct OFF entries : ${offKnowledge.entries.size}")
        println("Variant candidates : ${variantCandidates.size}")
        println("Added variants     : $added")
        println("Merged entries     : ${mergedEntries.size}")
        println("Output             : ${outputFile.absolutePath}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}