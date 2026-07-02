package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Test
import java.io.File

class OFFKnowledgeApplyPlanAnalysisTest {

    @Test
    fun analyzeApplyPlan() {

        val input =
            File(
                "data/generated/off/off_knowledge_apply_plan.json"
            )

        require(input.exists()) {
            "OFF apply plan not found: ${input.absolutePath}"
        }

        val json =
            JsonParser
                .parseString(
                    input.readText()
                )
                .asJsonObject

        val entries =
            json
                .getAsJsonArray("entries")

        val dimensionCounts =
            mutableMapOf<String, Int>()

        val sourceCounts =
            mutableMapOf<String, Int>()

        val catalogCounts =
            mutableMapOf<String, Int>()

        entries.forEach { element ->

            val entry =
                element.asJsonObject

            entry
                .stringOrNull("dimension")
                ?.let { dimension ->

                    dimensionCounts[dimension] =
                        (dimensionCounts[dimension] ?: 0) + 1
                }

            entry
                .stringOrNull("source")
                ?.let { source ->

                    sourceCounts[source] =
                        (sourceCounts[source] ?: 0) + 1
                }

            entry
                .stringOrNull("catalogNormalizedName")
                ?.let { name ->

                    catalogCounts[name] =
                        (catalogCounts[name] ?: 0) + 1
                }
        }

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OFF APPLY PLAN ANALYSIS")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Entries : ${entries.size()}")

        println()
        println("Dimensions:")
        printTopMap(
            dimensionCounts
        )

        println()
        println("Sources:")
        printTopMap(
            sourceCounts
        )

        println()
        println("Top Catalog Items:")
        printTopMap(
            catalogCounts,
            limit = 50
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

    private fun printTopMap(

        values: Map<String, Int>,

        limit: Int = 100

    ) {

        if (values.isEmpty()) {
            println("- none")
            return
        }

        values
            .entries
            .sortedWith(
                compareByDescending<Map.Entry<String, Int>> {
                    it.value
                }.thenBy {
                    it.key
                }
            )
            .take(limit)
            .forEach { entry ->

                println(
                    "- ${entry.key}: ${entry.value}"
                )
            }
    }
}