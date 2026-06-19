package de.shopme.tools.knowledge.reader

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.shopme.domain.catalog.CatalogItem

class ResourceCatalogReader : CatalogReader {

    override fun read(): List<CatalogItem> {

        val stream =

            javaClass.classLoader!!

                .getResourceAsStream(

                    "catalog/supermarket_dataset.json"

                ) ?: error("catalog not found")

        val json =

            stream.bufferedReader()

                .use {

                    it.readText()

                }

        val type =

            object :
                TypeToken<List<CatalogItem>>() {}.type

        return Gson().fromJson(

            json,

            type

        )

    }

}