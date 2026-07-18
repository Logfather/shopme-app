package de.shopme.testing.system.tools.knowledge.report

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File

class CatalogJsonFileReader {

    fun read(
        file: File
    ): List<JsonObject> {

        val root =
            JsonParser
                .parseString(
                    file.readText()
                )

        val array =
            when {
                root.isJsonArray ->
                    root.asJsonArray

                root.isJsonObject ->
                    root.asJsonObject["items"]
                        .asJsonArray

                else ->
                    error(
                        "Unsupported catalog JSON"
                    )
            }

        return array.map {
            it.asJsonObject
        }
    }
}