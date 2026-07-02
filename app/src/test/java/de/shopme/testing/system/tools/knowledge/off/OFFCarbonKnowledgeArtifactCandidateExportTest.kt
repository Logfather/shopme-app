package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.off.OFFCarbonKnowledgeArtifactCandidateBuilder
import de.shopme.tools.knowledge.off.OFFCarbonKnowledgeImportCandidate
import de.shopme.tools.knowledge.off.OFFHivraExtract
import org.junit.Test
import java.io.File

class OFFCarbonKnowledgeArtifactCandidateExportTest {

    private val gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

    @Test
    fun exportCarbonKnowledgeArtifactCandidates() {

        val extractFile =
            File(
                "data/generated/off/off_hivra_extract.jsonl"
            )

        val carbonImportCandidateFile =
            File(
                "data/generated/off/off_carbon_import_candidates.json"
            )

        val outputFile =
            File(
                "data/generated/off/off_carbon_knowledge_artifact_candidates.json"
            )

        require(extractFile.exists()) {
            "OFF Hivra extract not found: ${extractFile.absolutePath}"
        }

        require(carbonImportCandidateFile.exists()) {
            "OFF carbon import candidates not found: ${carbonImportCandidateFile.absolutePath}"
        }

        val carbonImportCandidates =
            gson.fromJson<List<OFFCarbonKnowledgeImportCandidate>>(
                carbonImportCandidateFile.readText(),
                object : TypeToken<List<OFFCarbonKnowledgeImportCandidate>>() {}.type
            )

        val requiredReferences =
            carbonImportCandidates
                .map {
                    it.reference
                }
                .toSet()

        val matchingExtracts =
            loadMatchingExtracts(
                file = extractFile,
                requiredReferences = requiredReferences
            )

        val artifactCandidates =
            OFFCarbonKnowledgeArtifactCandidateBuilder()
                .build(
                    carbonCandidates = carbonImportCandidates,
                    extracts = matchingExtracts
                )

        outputFile.parentFile?.mkdirs()

        outputFile.writeText(
            gson.toJson(
                artifactCandidates
            )
        )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OFF CARBON KNOWLEDGE ARTIFACT CANDIDATES")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Matching extracts         : ${matchingExtracts.size}")
        println("Carbon import candidates  : ${carbonImportCandidates.size}")
        println("Artifact candidates       : ${artifactCandidates.size}")
        println("Output                    : ${outputFile.absolutePath}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    private fun loadMatchingExtracts(
        file: File,
        requiredReferences: Set<String>
    ): List<OFFHivraExtract> {

        val result =
            mutableListOf<OFFHivraExtract>()

        file
            .bufferedReader()
            .useLines { lines ->

                lines.forEach { line ->

                    if (line.isBlank()) {
                        return@forEach
                    }

                    val extract =
                        parseExtract(line)
                            ?: return@forEach

                    val code =
                        extract.code
                            ?: return@forEach

                    val reference =
                        "off:$code"

                    if (reference in requiredReferences) {
                        result += extract
                    }
                }
            }

        return result
    }

    private fun parseExtract(
        line: String
    ): OFFHivraExtract? {

        return runCatching {
            gson.fromJson(
                line,
                OFFHivraExtract::class.java
            )
        }.getOrNull()
    }
}