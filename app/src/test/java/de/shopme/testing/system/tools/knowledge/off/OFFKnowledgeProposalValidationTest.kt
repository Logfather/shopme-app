package de.shopme.testing.system.tools.knowledge.gap

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import de.shopme.tools.knowledge.off.OFFKnowledgeProposal
import de.shopme.tools.knowledge.off.OFFKnowledgeProposalValidator
import org.junit.Test
import java.io.File

class OFFKnowledgeProposalValidationTest {

    private val gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

    @Test
    fun validateKnowledgeProposals() {

        val inputFile =
            File(
                "build/off/off_knowledge_proposals.json"
            )

        val invalidOutputFile =
            File(
                "build/off/off_invalid_knowledge_proposals.json"
            )

        require(inputFile.exists()) {
            "OFF knowledge proposals not found: ${inputFile.absolutePath}"
        }

        val proposals =
            loadProposals(inputFile)

        val result =
            OFFKnowledgeProposalValidator()
                .validate(proposals)

        invalidOutputFile.parentFile?.mkdirs()

        invalidOutputFile.writeText(
            gson.toJson(result.invalid)
        )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OFF KNOWLEDGE PROPOSAL VALIDATION")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Proposals : ${proposals.size}")
        println("Valid     : ${result.valid.size}")
        println("Invalid   : ${result.invalid.size}")
        println("Invalid output : ${invalidOutputFile.absolutePath}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        check(result.invalid.isEmpty()) {
            "Invalid OFF knowledge proposals found: ${result.invalid.size}"
        }
    }

    private fun loadProposals(
        file: File
    ): List<OFFKnowledgeProposal> {

        val json =
            JsonParser
                .parseString(
                    file.readText()
                )
                .asJsonArray

        return json
            .mapNotNull { element ->

                runCatching {
                    gson.fromJson(
                        element,
                        OFFKnowledgeProposal::class.java
                    )
                }.getOrNull()
            }
    }
}