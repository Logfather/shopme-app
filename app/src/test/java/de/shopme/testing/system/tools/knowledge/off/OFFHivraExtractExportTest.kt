package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import de.shopme.tools.knowledge.data.KnowledgeDataDirectories
import de.shopme.tools.knowledge.off.OFFHivraExtractMapper
import java.io.File
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream
import kotlin.test.Test


class OFFHivraExtractExportTest {

    private val gson =

        GsonBuilder()

            .disableHtmlEscaping()

            .create()

    @Test
    fun exportHivraExtract() {

        val input =
            File(
                KnowledgeDataDirectories.openFoodFactsRaw,
                "off-products.jsonl.gz"
            )

        val output =
            File(
                        "data/generated/off/off_hivra_extract.jsonl"
                    )

        require(input.exists()) {
            "Open Food Facts dump not found: ${input.absolutePath}"
        }

        output.parentFile?.mkdirs()

        val mapper =
            OFFHivraExtractMapper()

        var scanned =
            0

        var exported =
            0

        output.bufferedWriter().use { writer ->

            GZIPInputStream(
                input.inputStream()
            ).use { gzip ->

                InputStreamReader(gzip)
                    .buffered()
                    .useLines { lines ->

                        lines.forEach { line ->

                            scanned++

                            val product =
                                runCatching {
                                    JsonParser
                                        .parseString(line)
                                        .asJsonObject
                                }.getOrNull()
                                    ?: return@forEach

                            val extract =
                                mapper.map(product)
                                    ?: return@forEach

                            writer.write(
                                gson.toJson(extract)
                            )

                            writer.newLine()

                            exported++

                            if (scanned % 100_000 == 0) {
                                println(
                                    "OFF scanned=$scanned exported=$exported"
                                )
                            }
                        }
                    }
            }
        }

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OPEN FOOD FACTS HIVRA EXTRACT")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Scanned : $scanned")
        println("Exported: $exported")
        println("Output  : ${output.absolutePath}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}