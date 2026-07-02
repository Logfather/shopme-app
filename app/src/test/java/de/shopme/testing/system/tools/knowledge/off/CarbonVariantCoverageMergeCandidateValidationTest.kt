package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.off.CarbonVariantCoverageMergeCandidate
import org.junit.Test
import java.io.File

class CarbonVariantCoverageMergeCandidateValidationTest {

    private val gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

    @Test
    fun validateCarbonVariantCoverageMergeCandidates() {

        val input =
            File(
                "data/generated/off/carbon_variant_coverage_merge_candidates.json"
            )

        val invalidOutput =
            File(
                "data/generated/off/invalid_carbon_variant_coverage_merge_candidates.json"
            )

        require(input.exists()) {
            "Carbon variant merge candidates not found: ${input.absolutePath}"
        }

        val type =
            object : TypeToken<List<CarbonVariantCoverageMergeCandidate>>() {}.type

        val candidates: List<CarbonVariantCoverageMergeCandidate> =
            gson.fromJson(
                input.readText(),
                type
            )

        val invalid =
            candidates.filter { candidate ->

                candidate.catalogNormalizedName.isBlank() ||
                        candidate.resolvedCarbonReference.isBlank() ||
                        candidate.kilogramsPerKilogram <= 0.0 ||
                        candidate.source.isBlank()
            }

        invalidOutput.parentFile?.mkdirs()

        invalidOutput.writeText(
            gson.toJson(
                invalid
            )
        )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 CARBON VARIANT MERGE VALIDATION")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Candidates : ${candidates.size}")
        println("Valid      : ${candidates.size - invalid.size}")
        println("Invalid    : ${invalid.size}")
        println("Invalid output : ${invalidOutput.absolutePath}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}