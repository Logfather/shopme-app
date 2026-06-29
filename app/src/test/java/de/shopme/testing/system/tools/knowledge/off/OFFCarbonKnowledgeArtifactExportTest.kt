package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.carbon.CarbonFootprint
import de.shopme.tools.knowledge.carbon.CarbonKnowledge
import de.shopme.tools.knowledge.off.OFFCarbonKnowledgeArtifactCandidate
import org.junit.Test
import java.io.File

class OFFCarbonKnowledgeArtifactExportTest {

    private val gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

    @Test
    fun exportCarbonKnowledgeArtifact() {

        val input =
            File(
                "build/off/off_carbon_knowledge_artifact_candidates.json"
            )

        val output =
            File(
                "build/off/carbon_footprint_from_off.json"
            )

        require(input.exists()) {
            "OFF carbon artifact candidates not found: ${input.absolutePath}"
        }

        val candidates =
            gson.fromJson<List<OFFCarbonKnowledgeArtifactCandidate>>(
                input.readText(),
                object : TypeToken<List<OFFCarbonKnowledgeArtifactCandidate>>() {}.type
            )

        val entries =
            candidates
                .associate { candidate ->

                    candidate.catalogNormalizedName to
                            CarbonFootprint(
                                kilogramsPerKilogram =
                                    candidate.kilogramsCo2PerKilogram
                            )
                }
                .toSortedMap()

        val knowledge =
            CarbonKnowledge(
                entries = entries
            )

        output.parentFile?.mkdirs()

        output.writeText(
            gson.toJson(
                knowledge
            )
        )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OFF CARBON KNOWLEDGE ARTIFACT")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Candidates : ${candidates.size}")
        println("Entries    : ${entries.size}")
        println("Output     : ${output.absolutePath}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}