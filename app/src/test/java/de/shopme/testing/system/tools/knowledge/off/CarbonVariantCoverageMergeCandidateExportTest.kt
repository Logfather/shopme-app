package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.off.CarbonVariantCoverageCandidate
import de.shopme.tools.knowledge.off.CarbonVariantCoverageMergeCandidateBuilder
import de.shopme.tools.knowledge.off.OFFCarbonKnowledgeArtifactCandidate
import org.junit.Test
import java.io.File

class CarbonVariantCoverageMergeCandidateExportTest {

    private val gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

    @Test
    fun exportCarbonVariantCoverageMergeCandidates() {

        val variantFile =
            File(
                "data/generated/off/carbon_variant_coverage_safe.json"
            )

        val carbonFile =
            File(
                "data/generated/off/off_carbon_knowledge_artifact_candidates.json"
            )

        val outputFile =
            File(
                "data/generated/off/carbon_variant_coverage_merge_candidates.json"
            )

        require(variantFile.exists()) {
            "Carbon variant coverage safe file not found: ${variantFile.absolutePath}"
        }

        require(carbonFile.exists()) {
            "OFF carbon artifact candidates not found: ${carbonFile.absolutePath}"
        }

        val variantType =
            object : TypeToken<List<CarbonVariantCoverageCandidate>>() {}.type

        val carbonType =
            object : TypeToken<List<OFFCarbonKnowledgeArtifactCandidate>>() {}.type

        val variantCandidates: List<CarbonVariantCoverageCandidate> =
            gson.fromJson(
                variantFile.readText(),
                variantType
            )

        val carbonCandidates: List<OFFCarbonKnowledgeArtifactCandidate> =
            gson.fromJson(
                carbonFile.readText(),
                carbonType
            )

        val mergeCandidates =
            CarbonVariantCoverageMergeCandidateBuilder()
                .build(
                    variantCandidates = variantCandidates,
                    carbonCandidates = carbonCandidates
                )

        outputFile.parentFile?.mkdirs()

        outputFile.writeText(
            gson.toJson(
                mergeCandidates
            )
        )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 CARBON VARIANT MERGE CANDIDATES")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Safe variants    : ${variantCandidates.size}")
        println("Carbon entries   : ${carbonCandidates.size}")
        println("Merge candidates : ${mergeCandidates.size}")
        println("Output           : ${outputFile.absolutePath}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}