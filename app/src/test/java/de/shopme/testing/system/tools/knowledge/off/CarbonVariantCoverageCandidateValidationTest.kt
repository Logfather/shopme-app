package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.off.CarbonVariantCoverageCandidate
import de.shopme.tools.knowledge.off.CarbonVariantCoverageCandidateValidator
import org.junit.Test
import java.io.File

class CarbonVariantCoverageCandidateValidationTest {

    private val gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

    @Test
    fun validateCarbonVariantCoverageCandidates() {

        val input =
            File(
                "build/off/carbon_variant_coverage_candidates.json"
            )

        val safeOutput =
            File(
                "build/off/carbon_variant_coverage_safe.json"
            )

        val reviewOutput =
            File(
                "build/off/carbon_variant_coverage_review.json"
            )

        val invalidOutput =
            File(
                "build/off/carbon_variant_coverage_invalid.json"
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

        val result =
            CarbonVariantCoverageCandidateValidator()
                .validate(
                    candidates = candidates
                )

        safeOutput.parentFile?.mkdirs()

        safeOutput.writeText(
            gson.toJson(
                result.safe
            )
        )

        reviewOutput.writeText(
            gson.toJson(
                result.review
            )
        )

        invalidOutput.writeText(
            gson.toJson(
                result.invalid
            )
        )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 CARBON VARIANT COVERAGE VALIDATION")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Candidates : ${candidates.size}")
        println("Safe       : ${result.safe.size}")
        println("Review     : ${result.review.size}")
        println("Invalid    : ${result.invalid.size}")
        println("Safe output    : ${safeOutput.absolutePath}")
        println("Review output  : ${reviewOutput.absolutePath}")
        println("Invalid output : ${invalidOutput.absolutePath}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}