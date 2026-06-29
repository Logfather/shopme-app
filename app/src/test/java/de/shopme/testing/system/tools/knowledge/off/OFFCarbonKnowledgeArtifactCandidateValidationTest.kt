package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.off.OFFCarbonKnowledgeArtifactCandidate
import de.shopme.tools.knowledge.off.OFFCarbonKnowledgeArtifactCandidateValidator
import org.junit.Test
import java.io.File

class OFFCarbonKnowledgeArtifactCandidateValidationTest {

    private val gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

    @Test
    fun validateCarbonKnowledgeArtifactCandidates() {

        val input =
            File(
                "build/off/off_carbon_knowledge_artifact_candidates.json"
            )

        val invalidOutput =
            File(
                "build/off/off_invalid_carbon_knowledge_artifact_candidates.json"
            )

        require(input.exists()) {
            "OFF carbon artifact candidates not found: ${input.absolutePath}"
        }

        val candidates =
            gson.fromJson<List<OFFCarbonKnowledgeArtifactCandidate>>(
                input.readText(),
                object : TypeToken<List<OFFCarbonKnowledgeArtifactCandidate>>() {}.type
            )

        val validator =
            OFFCarbonKnowledgeArtifactCandidateValidator()

        val valid =
            mutableListOf<OFFCarbonKnowledgeArtifactCandidate>()

        val invalid =
            mutableListOf<OFFCarbonKnowledgeArtifactCandidate>()

        candidates.forEach { candidate ->

            if (
                validator.isValid(candidate)
            ) {
                valid += candidate
            } else {
                invalid += candidate
            }
        }

        invalidOutput.parentFile?.mkdirs()

        invalidOutput.writeText(
            gson.toJson(
                invalid
            )
        )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OFF CARBON ARTIFACT VALIDATION")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Candidates : ${candidates.size}")
        println("Valid      : ${valid.size}")
        println("Invalid    : ${invalid.size}")
        println("Invalid output : ${invalidOutput.absolutePath}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}