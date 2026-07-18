package de.shopme.tools.knowledge.off.loader

import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class OpenFoodFactsChunkSplitter {

    fun split(
        inputFile: File,
        outputDirectory: File,
        recordsPerChunk: Int = 500_000
    ): SplitResult {
        require(inputFile.exists()) {
            "OFF dump not found: ${inputFile.absolutePath}"
        }

        require(recordsPerChunk > 0) {
            "recordsPerChunk must be greater than zero."
        }

        outputDirectory.mkdirs()

        println("Output directory: ${outputDirectory.absolutePath}")
        println("Exists: ${outputDirectory.exists()}")

        var totalRecords = 0
        var chunkIndex = 0
        var recordsInCurrentChunk = 0

        var writer: BufferedWriter? = null

        fun openNextChunk(): BufferedWriter {
            writer?.close()

            chunkIndex++
            recordsInCurrentChunk = 0

            val chunkFile =
                File(
                    outputDirectory,
                    "off-products-${chunkIndex.toString().padStart(5, '0')}.jsonl.gz"
                )

            println("Creating chunk: ${chunkFile.absolutePath}")

            chunkFile.parentFile.mkdirs()

            return BufferedWriter(
                OutputStreamWriter(
                    GZIPOutputStream(
                        chunkFile.outputStream()
                    )
                )
            )
        }

        GZIPInputStream(
            inputFile.inputStream()
        ).bufferedReader().use { reader ->

            try {
                while (true) {
                    val line =
                        reader.readLine()
                            ?: break

                    if (line.isBlank()) {
                        continue
                    }

                    if (writer == null || recordsInCurrentChunk >= recordsPerChunk) {
                        writer = openNextChunk()
                    }

                    writer!!.write(line)
                    writer!!.newLine()

                    totalRecords++
                    recordsInCurrentChunk++

                    if (totalRecords % 100_000 == 0) {
                        println("OFF split records=$totalRecords chunks=$chunkIndex")
                    }
                }
            } finally {
                writer?.close()
            }
        }

        return SplitResult(
            totalRecords = totalRecords,
            chunkCount = chunkIndex,
            outputDirectory = outputDirectory
        )
    }
}

data class SplitResult(
    val totalRecords: Int,
    val chunkCount: Int,
    val outputDirectory: File
)