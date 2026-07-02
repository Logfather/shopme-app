package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.off.OFFCarbonKnowledgeArtifactCandidate
import org.junit.Test
import java.io.File

class OFFCarbonKnowledgeArtifactCandidateAnalysisTest {

    private val gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

    @Test
    fun analyzeCarbonKnowledgeArtifactCandidates() {

        val input =
            File(
                "data/generated/off/off_carbon_knowledge_artifact_candidates.json"
            )

        require(input.exists()) {
            "OFF carbon artifact candidates not found: ${input.absolutePath}"
        }

        val candidates =
            gson.fromJson<List<OFFCarbonKnowledgeArtifactCandidate>>(
                input.readText(),
                object : TypeToken<List<OFFCarbonKnowledgeArtifactCandidate>>() {}.type
            )

        if (candidates.isEmpty()) {

            println()
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            println("🧠 OFF CARBON ARTIFACT ANALYSIS")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            println("Candidates : 0")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            return
        }

        val sortedByCarbon =
            candidates.sortedBy {
                it.kilogramsCo2PerKilogram
            }

        val min =
            sortedByCarbon.first()

        val max =
            sortedByCarbon.last()

        val average =
            candidates
                .map {
                    it.kilogramsCo2PerKilogram
                }
                .average()

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OFF CARBON ARTIFACT ANALYSIS")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Candidates : ${candidates.size}")
        println(
            "Average CO₂e: %.2fkg CO₂e/kg product"
                .format(average)
        )

        println()
        println("Lowest CO₂:")
        println(
            "- ${min.catalogNormalizedName}: " +
                    "${"%.2f".format(min.kilogramsCo2PerKilogram)}kg CO₂e/kg product"
        )

        println()
        println("Highest CO₂:")
        println(
            "- ${max.catalogNormalizedName}: " +
                    "${"%.2f".format(max.kilogramsCo2PerKilogram)}kg CO₂e/kg product"
        )

        println()
        println("Top 20 Highest:")

        sortedByCarbon
            .takeLast(20)
            .reversed()
            .forEach {

                println(
                    "- ${it.catalogNormalizedName}: " +
                            "${"%.2f".format(it.kilogramsCo2PerKilogram)}kg CO₂e/kg product"
                )
            }

        println()
        println("Top 20 Lowest:")

        sortedByCarbon
            .take(20)
            .forEach {

                println(
                    "- ${it.catalogNormalizedName}: " +
                            "${"%.2f".format(it.kilogramsCo2PerKilogram)}kg CO₂e/kg product"
                )
            }

        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}