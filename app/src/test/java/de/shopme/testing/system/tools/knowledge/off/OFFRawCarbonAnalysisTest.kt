package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Test
import java.io.File
import java.util.zip.GZIPInputStream

class OFFRawCarbonAnalysisTest {

    @Test
    fun analyzeRawCarbonData() {

        val input =
            File(
                "data/generated/openfoodfacts/openfoodfacts-products.jsonl.gz"
            )

        require(input.exists()) {
            "OFF dump not found: ${input.absolutePath}"
        }

        val counters =
            CarbonCounters()

        GZIPInputStream(
            input.inputStream()
        ).bufferedReader().useLines { lines ->

            lines.forEach { line ->

                if (line.isBlank()) {
                    return@forEach
                }

                val product =
                    runCatching {
                        JsonParser
                            .parseString(line)
                            .asJsonObject
                    }.getOrNull()
                        ?: return@forEach

                counters.total++

                analyzeContainer(
                    product.objectOrNull("ecoscore_data"),
                    counters
                )

                analyzeContainer(
                    product.objectOrNull("environmental_score_data"),
                    counters
                )

                if (counters.total % 100_000 == 0) {
                    println(
                        "Analyzed raw products=${counters.total}"
                    )
                }
            }
        }

        counters.print()
    }

    private fun analyzeContainer(
        container: JsonObject?,
        counters: CarbonCounters
    ) {

        if (container == null) {
            return
        }

        counters.containersPresent++

        val agribalyse =
            container.objectOrNull("agribalyse")
                ?: return

        counters.agribalysePresent++

        counters.co2Total.record(
            agribalyse.doubleOrNull("co2_total")
        )

        counters.co2Agriculture.record(
            agribalyse.doubleOrNull("co2_agriculture")
        )

        counters.co2Processing.record(
            agribalyse.doubleOrNull("co2_processing")
        )

        counters.co2Packaging.record(
            agribalyse.doubleOrNull("co2_packaging")
        )

        counters.co2Transportation.record(
            agribalyse.doubleOrNull("co2_transportation")
        )
    }

    private data class CarbonCounters(

        var total: Int = 0,

        var containersPresent: Int = 0,

        var agribalysePresent: Int = 0,

        val co2Total: CarbonFieldCounter =
            CarbonFieldCounter(),

        val co2Agriculture: CarbonFieldCounter =
            CarbonFieldCounter(),

        val co2Processing: CarbonFieldCounter =
            CarbonFieldCounter(),

        val co2Packaging: CarbonFieldCounter =
            CarbonFieldCounter(),

        val co2Transportation: CarbonFieldCounter =
            CarbonFieldCounter()
    ) {

        fun print() {

            println()
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            println("🧠 OFF RAW CARBON ANALYSIS")
            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            println("Products            : $total")
            println("Containers          : $containersPresent")
            println("Agribalyse          : $agribalysePresent")
            println()

            printField(
                "co2_total",
                co2Total
            )

            printField(
                "co2_agriculture",
                co2Agriculture
            )

            printField(
                "co2_processing",
                co2Processing
            )

            printField(
                "co2_packaging",
                co2Packaging
            )

            printField(
                "co2_transportation",
                co2Transportation
            )

            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        }

        private fun printField(
            name: String,
            counter: CarbonFieldCounter
        ) {

            println("$name")
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

        if (
            value.isJsonNull ||
            !value.isJsonObject
        ) {
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