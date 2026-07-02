package de.shopme.tools.knowledge.compiler.catalog

import com.google.gson.Gson
import de.shopme.domain.catalog.CatalogItem

class GsonCatalogJsonSerializer(
    private val gson: Gson
) : CatalogJsonSerializer {

    override fun serialize(
        catalog: List<CatalogItem>
    ): String {

        return gson.toJson(catalog)
    }
}