package de.shopme.data.datasource.catalog

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.shopme.domain.catalog.CatalogItem

object CatalogParser {

    fun parse(

        json: String

    ): List<CatalogItem> {

        val type =

            object : TypeToken<List<CatalogItem>>() {}.type

        return Gson().fromJson(

            json,

            type

        )

    }

}