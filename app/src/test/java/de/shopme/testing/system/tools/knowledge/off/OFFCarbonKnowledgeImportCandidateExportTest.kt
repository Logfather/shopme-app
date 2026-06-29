package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.off.OFFCarbonKnowledgeImportCandidateBuilder
import de.shopme.tools.knowledge.off.OFFKnowledgeImportCandidate
import org.junit.Test
import java.io.File

class OFFCarbonKnowledgeImportCandidateExportTest {

    private val gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

    @Test
    fun exportCarbonImportCandidates() {

        val input =
            File(
                "build/off/off_knowledge_import_candidates.json"
            )

        val output =
            File(
                "build/off/off_carbon_import_candidates.json"
            )

        require(input.exists()) {
            "OFF import candidates not found: ${input.absolutePath}"
        }

        val candidates =
            gson.fromJson<List<OFFKnowledgeImportCandidate>>(
                input.readText(),
                object : TypeToken<List<OFFKnowledgeImportCandidate>>() {}.type
            )

        val carbonCandidates =
            OFFCarbonKnowledgeImportCandidateBuilder()
                .build(
                    candidates = candidates
                )

        output.parentFile?.mkdirs()

        output.writeText(
            gson.toJson(
                carbonCandidates
            )
        )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OFF CARBON IMPORT CANDIDATES")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Import candidates : ${candidates.size}")
        println("Carbon candidates : ${carbonCandidates.size}")
        println("Output            : ${output.absolutePath}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}