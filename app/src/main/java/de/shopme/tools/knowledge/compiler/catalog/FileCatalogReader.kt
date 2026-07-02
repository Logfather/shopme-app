package de.shopme.tools.knowledge.compiler.catalog

import de.shopme.domain.catalog.CatalogItem
import java.io.File

class FileCatalogReader(
    private val deserializer: CatalogJsonDeserializer,
    private val inputFile: File
) {

    fun read(): List<CatalogItem> {

        return deserializer.deserialize(
            inputFile.readText()
        )
    }
}