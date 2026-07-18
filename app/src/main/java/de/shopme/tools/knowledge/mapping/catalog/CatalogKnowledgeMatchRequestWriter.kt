package de.shopme.tools.knowledge.mapping.catalog

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

class CatalogKnowledgeMatchRequestWriter(
    private val gson: Gson =
        GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create()
) {

    fun write(
        requests: CatalogKnowledgeMatchRequests,
        file: File
    ) {

        val parentDirectory =
            file.parentFile

        if (
            parentDirectory != null &&
            !parentDirectory.exists()
        ) {
            check(
                parentDirectory.mkdirs()
            ) {
                "Could not create match request directory: " +
                        parentDirectory.absolutePath
            }
        }

        file.writeText(
            gson.toJson(requests)
        )
    }
}