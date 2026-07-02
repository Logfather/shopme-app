package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import de.shopme.tools.knowledge.off.OFFKnowledgeCandidate
import de.shopme.tools.knowledge.off.OFFKnowledgeProposalBuilder
import org.junit.Test
import java.io.File

class OFFKnowledgeProposalExportTest {

    private val gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

    @Test
    fun exportKnowledgeProposals() {

        val inputFile =
            File(
                "data/generated/off/off_knowledge_candidates.json"
            )

        val outputFile =
            File(
                "data/generated/off/off_knowledge_proposals.json"
            )

        require(inputFile.exists()) {
            "OFF knowledge candidates not found: ${inputFile.absolutePath}"
        }

        val candidates =
            loadCandidates(inputFile)

        val proposals =
            OFFKnowledgeProposalBuilder()
                .build(candidates)
                .sortedWith(
                    compareBy(
                        { it.catalogNormalizedName },
                        { it.offProductName }
                    )
                )

        outputFile.parentFile?.mkdirs()

        outputFile.writeText(
            gson.toJson(proposals)
        )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OPEN FOOD FACTS KNOWLEDGE PROPOSALS")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Candidates : ${candidates.size}")
        println("Proposals  : ${proposals.size}")
        println("Output     : ${outputFile.absolutePath}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    private fun loadCandidates(
        file: File
    ): List<OFFKnowledgeCandidate> {

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
                        OFFKnowledgeCandidate::class.java
                    )
                }.getOrNull()
            }
    }
}