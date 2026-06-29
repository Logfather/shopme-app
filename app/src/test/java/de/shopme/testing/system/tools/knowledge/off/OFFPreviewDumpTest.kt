package de.shopme.testing.system.tools.knowledge.off

import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.test.Test

class OFFPreviewDumpTest {

    @Test
    fun createPreviewDump() {

        val input =
            File(
                "build/input/off-products.jsonl.gz"
            )

        require(input.exists()) {
            "OFF dump not found: ${input.absolutePath}"
        }

        val output =
            File(
                "build/input/off-products-preview-50k.jsonl.gz"
            )

        output.parentFile.mkdirs()

        var exported = 0

        GZIPInputStream(
            input.inputStream()
        ).bufferedReader().use { reader ->

            GZIPOutputStream(
                output.outputStream()
            ).use { gzip ->

                BufferedWriter(
                    OutputStreamWriter(gzip)
                ).use { writer ->

                    while (exported < 50_000) {

                        val line =
                            reader.readLine()
                                ?: break

                        writer.write(line)
                        writer.newLine()

                        exported++

                        if (exported % 10_000 == 0) {

                            println(
                                "Preview exported=$exported"
                            )
                        }
                    }
                }
            }
        }

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OFF PREVIEW DUMP")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Products : $exported")
        println("Output   : ${output.absolutePath}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}