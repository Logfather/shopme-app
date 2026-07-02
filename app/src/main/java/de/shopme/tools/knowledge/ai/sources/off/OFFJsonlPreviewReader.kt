package de.shopme.tools.knowledge.ai.sources.off

import com.google.gson.Gson
import java.io.File
import java.util.zip.GZIPInputStream
import kotlin.jvm.java

class OFFJsonlPreviewReader(
    private val gson: Gson = Gson()
) {

    fun read(
        file: File,
        limit: Int
    ): List<OFFRawProduct> {

        require(limit > 0) {
            "Limit must be greater than zero."
        }

        return GZIPInputStream(
            file.inputStream()
        ).bufferedReader()
            .useLines { lines ->
                lines
                    .filter { it.isNotBlank() }
                    .take(limit)
                    .map { json ->
                        gson.fromJson(
                            json,
                            OFFJsonProduct::class.java
                        ).toRawProduct()
                    }
                    .toList()
            }
    }
}