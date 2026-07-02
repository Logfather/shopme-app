package de.shopme.tools.knowledge.compiler.catalog

import de.shopme.domain.catalog.CatalogItem
import java.io.File

class JsonCatalogWriter(
    private val serializer: CatalogJsonSerializer,
    private val outputFile: File
) : CatalogWriter {

    override fun write(catalog: List<CatalogItem>) {

        outputFile.writeText(
            serializer.serialize(catalog)
        )
    }
}