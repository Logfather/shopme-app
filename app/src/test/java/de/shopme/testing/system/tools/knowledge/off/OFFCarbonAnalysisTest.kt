package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Test
import java.io.File

class OFFCarbonAnalysisTest {

    @Test
    fun analyzeCarbonData() {

        val input =
            File(
                "build/off/off_hivra_extract.jsonl"
            )

        require(input.exists()) {
            "Hivra OFF extract not found: ${input.absolutePath}"
        }

        val counters =
            CarbonCounters()

        input
            .bufferedReader()
            .useLines { lines ->

                lines.forEach { line ->

                    if (line.isBlank()) {
                        return@forEach
                    }

                    val json =
                        runCatching {
                            JsonParser
                                .parseString(line)
                                .asJsonObject
                        }.getOrNull()
                            ?: return@forEach

                    counters.total++

                    val ecoscore =
                        json.objectOrNull("ecoscoreData")

                    val environmentalScore =
                        json.objectOrNull("environmentalScoreData")

                    counters.record(
                        ecoscore
                    )

                    counters.record(
                        environmentalScore
                    )

                    if (counters.total % 100_000 == 0) {
                        println(
                            "Analyzed carbon entries=${counters.total}"
                        )
                    }
                }
            }

        counters.print()
    }

    private data class CarbonCounters(

        var total: Int = 0,

        var containersPresent: Int = 0,

        var agribalysePresent: Int = 0,

        val co2Total: CarbonFieldCounter = CarbonFieldCounter(),

        val co2Agriculture: CarbonFieldCounter = CarbonFieldCounter(),

        val co2Processing: CarbonFieldCounter = CarbonFieldCounter(),

        val co2Packaging: CarbonFieldCounter = CarbonFieldCounter(),

        val co2Transportation: CarbonFieldCounter = CarbonFieldCounter()
    ) {

        private fun readDouble(
            json: JsonObject,
            key: String
        ): Double? {

            val value =
                json.get(key)
                    ?: return null

            if (value.isJsonNull) {
                return null
            }

            return runCatching {
                value.asDouble
            }.getOrNull()
        }

        private fun JsonObject.objectOrNull(
            key: String
        ): JsonObject? {

            val value =
                get(key)
                    ?: return null

            if (
                value.isJsonNull ||
                !value.isJsonObject
            ) {
                return null
            }

            return value.asJsonObject
        }

        fun record(
            container: JsonObject?
        ) {

            if (container == null) {
                return
            }

            containersPresent++

            val agribalyse =
                container.objectOrNull("agribalyse")
                    ?: return

            agribalysePresent++

            co2Total.record(
                readDouble(
                    agribalyse,
                    "co2_total"
                )
            )

            co2Agriculture.record(
                readDouble(
                    agribalyse,
                    "co2_agriculture"
                )
            )

            co2Processing.record(
                readDouble(
                    agribalyse,
                    "co2_processing"
                )
            )

            co2Packaging.record(
                readDouble(
                    agribalyse,
                    "co2_packaging"
                )
            )

            co2Transportation.record(
                readDouble(
                    agribalyse,
                    "co2_transportation"
                )
            )
        }

        fun print() {

            println()
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            println("🧠 OPEN FOOD FACTS CARBON ANALYSIS")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            println("Entries              : $total")
            println("Carbon containers    : $containersPresent")
            println("Agribalyse present   : $agribalysePresent")
            println()

            printField(
                name = "co2_total",
                counter = co2Total
            )

            printField(
                name = "co2_agriculture",
                counter = co2Agriculture
            )

            printField(
                name = "co2_processing",
                counter = co2Processing
            )

            printField(
                name = "co2_packaging",
                counter = co2Packaging
            )

            printField(
                name = "co2_transportation",
                counter = co2Transportation
            )

            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        }

        private fun printField(
            name: String,
            counter: CarbonFieldCounter
        ) {

            println("$name:")
            println("  present : ${counter.present}")
            println("  zero    : ${counter.zero}")
            println("  positive: ${counter.positive}")
            println("  negative: ${counter.negative}")
            println()
        }
    }

    private data class CarbonFieldCounter(

        var present: Int = 0,

        var zero: Int = 0,

        var positive: Int = 0,

        var negative: Int = 0
    ) {

        fun record(
            value: Double?
        ) {

            if (value == null) {
                return
            }

            present++

            when {
                value > 0.0 -> positive++
                value < 0.0 -> negative++
                else -> zero++
            }
        }
    }

    private fun JsonObject.objectOrNull(
        key: String
    ): JsonObject? {

        val value =
            get(key)
                ?: return null

        if (value.isJsonNull || !value.isJsonObject) {
            return null
        }

        return value.asJsonObject
    }

    private fun JsonObject.doubleOrNull(
        key: String
    ): Double? {

        val value =
            get(key)
                ?: return null

        if (value.isJsonNull) {
            return null
        }

        return runCatching {
            value.asDouble
        }.getOrNull()
    }
}