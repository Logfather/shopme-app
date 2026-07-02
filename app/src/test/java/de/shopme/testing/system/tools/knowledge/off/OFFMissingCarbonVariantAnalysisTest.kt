package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.off.CarbonBaseFoodVariantResolver
import de.shopme.tools.knowledge.off.OFFCarbonKnowledgeArtifactCandidate
import de.shopme.tools.knowledge.reader.ResourceCatalogReader
import org.junit.Test
import java.io.File

class OFFMissingCarbonVariantAnalysisTest {

    private val gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

    @Test
    fun analyzeVariantCoverage() {

        val resolver =
            CarbonBaseFoodVariantResolver()

        val carbonFile =
            File(
                "data/generated/off/off_carbon_knowledge_artifact_candidates.json"
            )

        require(carbonFile.exists()) {
            "OFF carbon artifact candidates not found: ${carbonFile.absolutePath}"
        }

        val type =
            object : TypeToken<List<OFFCarbonKnowledgeArtifactCandidate>>() {}.type

        val candidates: List<OFFCarbonKnowledgeArtifactCandidate> =
            gson.fromJson(
                carbonFile.readText(),
                type
            )

        val covered =
            candidates
                .map {
                    it.catalogNormalizedName
                }
                .toSet()

        val catalog =
            ResourceCatalogReader()
                .read()

        val missing =
            catalog
                .map { it.normalized }
                .filterNot { it in covered }
                .distinct()

        val recovered =
            mutableListOf<Pair<String, String>>()

        missing.forEach { product ->

            val resolved =

                resolver.resolve(
                    normalizedName = product,
                    coveredCarbonNames = covered
                )

            if (resolved != null) {

                recovered +=
                    product to resolved
            }
        }

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OFF MISSING CARBON VARIANTS")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Missing products : ${missing.size}")
        println("Recoverable      : ${recovered.size}")

        println()
        println("Top Matches:")

        recovered
            .take(100)
            .forEach {

                println(
                    "- ${it.first} -> ${it.second}"
                )
            }

        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}