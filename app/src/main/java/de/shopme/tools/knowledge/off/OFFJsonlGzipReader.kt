package de.shopme.tools.knowledge.off

import java.io.File
import java.util.zip.GZIPInputStream

class OFFJsonlGzipReader {

    fun read(
        input: File
    ): List<String> {

        check(input.exists()) {
            "OFF input file not found: ${input.absolutePath}"
        }

        return GZIPInputStream(
            input.inputStream()
        ).bufferedReader()
            .useLines { lines ->

                lines
                    .filter(String::isNotBlank)
                    .toList()
            }
    }
}