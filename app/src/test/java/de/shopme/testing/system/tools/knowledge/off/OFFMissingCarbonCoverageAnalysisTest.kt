package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.off.OFFCarbonKnowledgeArtifactCandidate
import de.shopme.tools.knowledge.reader.ResourceCatalogReader
import org.junit.Test
import java.io.File

class OFFMissingCarbonCoverageAnalysisTest {

    private val gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

    @Test
    fun analyzeMissingCarbonCoverage() {

        val carbonFile =
            File(
                "data/generated/off/off_carbon_knowledge_artifact_candidates.json"
            )

        require(carbonFile.exists()) {
            "OFF carbon artifact candidates not found: ${carbonFile.absolutePath}"
        }

        val catalog =
            ResourceCatalogReader()
                .read()

        val type =
            object : TypeToken<List<OFFCarbonKnowledgeArtifactCandidate>>() {}.type

        val candidates: List<OFFCarbonKnowledgeArtifactCandidate> =
            gson.fromJson(
                carbonFile.readText(),
                type
            )

        val covered: Set<String> =
            candidates
                .map {
                    it.catalogNormalizedName
                }
                .toSet()

        val missing: List<String> =
            catalog
                .map {
                    it.normalized
                }
                .filterNot {
                    it in covered
                }
                .sorted()

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OFF MISSING CARBON COVERAGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Catalog products : ${catalog.size}")
        println("Covered          : ${covered.size}")
        println("Missing          : ${missing.size}")

        println()
        println("First 200 Missing:")

        missing
            .take(200)
            .forEach {
                println("- $it")
            }

        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}