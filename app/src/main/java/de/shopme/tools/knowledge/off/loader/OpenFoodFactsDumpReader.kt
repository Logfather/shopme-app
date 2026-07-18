package de.shopme.tools.knowledge.off.loader

import java.io.BufferedReader
import java.io.File
import java.util.zip.GZIPInputStream

class OpenFoodFactsDumpReader {

    fun readLines(
        file: File,
        maxRecords: Int? = null
    ): List<String> {
        require(file.exists()) {
            "OFF dump not found: ${file.absolutePath}"
        }

        val lines = mutableListOf<String>()

        forEachLine(
            file = file,
            maxRecords = maxRecords
        ) { line ->
            lines += line
        }

        return lines
    }

    fun countLines(
        file: File
    ): Long {

        var count = 0L

        forEachLine(file) {
            count++
        }

        return count
    }

    fun forEachLine(
        file: File,
        maxRecords: Int? = null,
        consumer: (String) -> Unit
    ) {
        require(file.exists()) {
            "OFF dump not found: ${file.absolutePath}"
        }

        require(maxRecords == null || maxRecords > 0) {
            "maxRecords must be null or greater than zero."
        }

        var count = 0

        GZIPInputStream(
            file.inputStream()
        ).bufferedReader().use { reader: BufferedReader ->

            while (true) {
                if (maxRecords != null && count >= maxRecords) {
                    break
                }

                val line =
                    reader.readLine()
                        ?: break

                if (line.isBlank()) {
                    continue
                }

                consumer(line)

                count++
            }
        }
    }
}