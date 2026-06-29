package de.shopme.testing.system.tools.openfoodfacts

import com.google.gson.JsonParser
import org.junit.Test
import java.io.File
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream

class OFFDumpPreviewTest {

    @Test
    fun exportFirst50Entries() {

        val input = File(
            "build/input/openfoodfacts-products.jsonl.gz"
        )

        val output = File(
            "build/openfoodfacts/first_50_entries.json"
        )

        output.parentFile.mkdirs()

        val entries =
            mutableListOf<String>()

        GZIPInputStream(
            input.inputStream()
        ).use { gzip ->

            InputStreamReader(gzip)
                .buffered()
                .useLines { lines ->

                    lines
                        .take(50)
                        .forEach { line ->

                            val json =

                                JsonParser
                                    .parseString(line)

                            entries +=

                                json.toString()
                        }
                }
        }

        output.writeText(

            "[\n" +

                    entries.joinToString(
                        ",\n"
                    ) +

                    "\n]"
        )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OPEN FOOD FACTS PREVIEW")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Entries : ${entries.size}")
        println("Output  : ${output.absolutePath}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}