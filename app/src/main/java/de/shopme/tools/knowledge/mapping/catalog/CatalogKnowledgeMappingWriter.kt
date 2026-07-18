package de.shopme.tools.knowledge.mapping.catalog

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

class CatalogKnowledgeMappingWriter(
    private val gson: Gson =
        GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create()
) {

    fun write(
        mappings: CatalogKnowledgeMappings,
        file: File
    ) {

        val parentDirectory =
            file.parentFile

        if (
            parentDirectory != null &&
            !parentDirectory.exists()
        ) {
            check(parentDirectory.mkdirs()) {
                "Could not create mapping directory: " +
                        parentDirectory.absolutePath
            }
        }

        file.writeText(
            gson.toJson(mappings)
        )
    }
}