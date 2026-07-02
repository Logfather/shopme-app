package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Test
import java.io.File

class OFFKnowledgeCandidateAnalysisTest {

    @Test
    fun analyzeKnowledgeCandidates() {

        val input =
            File(
                "data/generated/openfoodfactsoff_knowledge_candidates.json"
            )

        require(input.exists()) {
            "OFF knowledge candidates not found: ${input.absolutePath}"
        }

        var total =
            0

        val dimensionCounts =
            mutableMapOf<String, Int>()

        val catalogCounts =
            mutableMapOf<String, Int>()

        val offProductCounts =
            mutableMapOf<String, Int>()

        val json =
            JsonParser
                .parseString(
                    input.readText()
                )
                .asJsonArray

        json.forEach { element ->

            val candidate =
                element.asJsonObject

            total++

            candidate
                .stringArray("dimensions")
                .forEach { dimension ->

                    dimensionCounts[dimension] =
                        (dimensionCounts[dimension] ?: 0) + 1
                }

            candidate
                .stringOrNull("catalogNormalizedName")
                ?.let { name ->

                    catalogCounts[name] =
                        (catalogCounts[name] ?: 0) + 1
                }

            candidate
                .stringOrNull("offProductName")
                ?.let { name ->

                    offProductCounts[name] =
                        (offProductCounts[name] ?: 0) + 1
                }
        }

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OFF KNOWLEDGE CANDIDATE ANALYSIS")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Candidates : $total")

        println()
        println("Dimensions:")
        printTopMap(
            dimensionCounts
        )

        println()
        println("Catalog Items with most candidates:")
        printTopMap(
            catalogCounts,
            limit = 30
        )

        println()
        println("OFF Products with most occurrences:")
        printTopMap(
            offProductCounts,
            limit = 30
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
            .filter {
                it.isNotBlank()
            }
    }

    private fun printTopMap(

        values: Map<String, Int>,

        limit: Int = 50

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
            .take(limit)
            .forEach { entry ->

                println(
                    "- ${entry.key}: ${entry.value}"
                )
            }
    }
}