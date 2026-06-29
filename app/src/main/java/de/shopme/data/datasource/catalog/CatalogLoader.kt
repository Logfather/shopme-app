package de.shopme.data.datasource.catalog

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.shopme.domain.catalog.CatalogItem
import de.shopme.domain.catalog.model.KnowledgeReferences

class CatalogLoader(
    private val context: Context
) {

    fun load(): List<CatalogItem> {

        val json = context.assets
            .open("catalog/supermarket_dataset.json")
            .bufferedReader()
            .use { it.readText() }

        val type = object : TypeToken<List<CatalogItem>>() {}.type

        val items: List<CatalogItem> =
            Gson().fromJson(json, type)

        return items.map { item ->

            if (item.knowledge == null) {

                item.copy(
                    knowledge = KnowledgeReferences()
                )

            } else {

                item
            }
        }
    }
}