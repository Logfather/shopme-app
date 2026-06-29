package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.off.CarbonVariantCoverageCandidateBuilder
import de.shopme.tools.knowledge.off.OFFCarbonKnowledgeArtifactCandidate
import de.shopme.tools.knowledge.reader.ResourceCatalogReader
import org.junit.Test
import java.io.File

class CarbonVariantCoverageCandidateExportTest {

    private val gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

    @Test
    fun exportCarbonVariantCoverageCandidates() {

        val carbonArtifactFile =
            File(
                "build/off/off_carbon_knowledge_artifact_candidates.json"
            )

        require(carbonArtifactFile.exists()) {
            "OFF carbon artifact candidates not found: ${carbonArtifactFile.absolutePath}"
        }

        val carbonCandidates =
            gson.fromJson<List<OFFCarbonKnowledgeArtifactCandidate>>(
                carbonArtifactFile.readText(),
                object : TypeToken<List<OFFCarbonKnowledgeArtifactCandidate>>() {}.type
            )

        val coveredCarbonNames =
            carbonCandidates
                .map {
                    it.catalogNormalizedName
                }
                .toSet()

        val catalog =
            ResourceCatalogReader()
                .read()

        val catalogNames =
            catalog
                .map {
                    it.normalized
                }

        val variantCandidates =
            CarbonVariantCoverageCandidateBuilder()
                .build(
                    catalogNormalizedNames = catalogNames,
                    coveredCarbonNames = coveredCarbonNames
                )

        val outputFile =
            File(
                "build/off/carbon_variant_coverage_candidates.json"
            )

        outputFile.parentFile?.mkdirs()

        outputFile.writeText(
            gson.toJson(
                variantCandidates
            )
        )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 CARBON VARIANT COVERAGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Catalog items      : ${catalogNames.size}")
        println("Covered carbon     : ${coveredCarbonNames.size}")
        println("Variant candidates : ${variantCandidates.size}")
        println("Output             : ${outputFile.absolutePath}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}