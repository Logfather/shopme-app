package de.shopme.tools.knowledge.compiler.catalog

import com.google.gson.Gson
import de.shopme.domain.catalog.CatalogItem

class GsonCatalogJsonDeserializer(
    private val gson: Gson
) : CatalogJsonDeserializer {

    override fun deserialize(
        json: String
    ): List<CatalogItem> {

        return gson.fromJson(
            json,
            Array<CatalogItem>::class.java
        ).toList()
    }
}