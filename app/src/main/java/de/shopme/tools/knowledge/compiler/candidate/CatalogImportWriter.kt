package de.shopme.tools.knowledge.compiler.candidate

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import de.shopme.domain.catalog.CatalogItem
import java.io.File

class CatalogImportWriter(
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
) {

    fun write(
        items: List<CatalogItem>,
        file: File
    ) {
        file.parentFile?.mkdirs()

        file.writeText(
            gson.toJson(items)
        )
    }
}