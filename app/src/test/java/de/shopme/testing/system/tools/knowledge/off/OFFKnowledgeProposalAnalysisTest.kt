package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Test
import java.io.File

class OFFKnowledgeProposalAnalysisTest {

    @Test
    fun analyzeKnowledgeProposals() {

        val input =
            File(
                "build/off/off_knowledge_proposals.json"
            )

        require(input.exists()) {
            "OFF knowledge proposals not found: ${input.absolutePath}"
        }

        val dimensionCounts =
            mutableMapOf<String, Int>()

        val sourceCounts =
            mutableMapOf<String, Int>()

        var total = 0

        val json =
            JsonParser
                .parseString(
                    input.readText()
                )
                .asJsonArray

        json.forEach { element ->

            val proposal =
                element.asJsonObject

            total++

            proposal
                .stringOrNull("source")
                ?.let { source ->

                    sourceCounts[source] =
                        (sourceCounts[source] ?: 0) + 1
                }

            proposal
                .stringArray("dimensions")
                .forEach { dimension ->

                    dimensionCounts[dimension] =
                        (dimensionCounts[dimension] ?: 0) + 1
                }
        }

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OFF KNOWLEDGE PROPOSAL ANALYSIS")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Proposals : $total")

        println()
        println("Dimensions:")
        printMap(
            dimensionCounts
        )

        println()
        println("Sources:")
        printMap(
            sourceCounts
        )

        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    private fun JsonObject.stringOrNull(
        key: String
    ): String? {

        val value =
            get(key)
                ?: return null

        if (value.isJsonNull) {
            return null
        }

        return runCatching {
            value.asString
        }.getOrNull()
            ?.takeIf {
                it.isNotBlank()
            }
    }

    private fun JsonObject.stringArray(
        key: String
    ): List<String> {

        val value =
            get(key)
                ?: return emptyList()

        if (
            value.isJsonNull ||
            !value.isJsonArray
        ) {
            return emptyList()
        }

        return value
            .asJsonArray
            .mapNotNull { element ->

                runCatching {
                    element.asString
                }.getOrNull()
            }
    }

    private fun printMap(
        values: Map<String, Int>
    ) {

        if (values.isEmpty()) {
            println("- none")
            return
        }

        values
            .entries
            .sortedByDescending {
                it.value
            }
            .forEach { entry ->

                println(
                    "- ${entry.key}: ${entry.value}"
                )
            }
    }
}