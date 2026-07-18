package de.shopme.tools.knowledge.nutrition

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.KnowledgeAssets

class NutritionAliasGraphLoader(

    private val context: Context

) {

    private val gson =
        Gson()

    fun load(): Map<String, String> {

        val json =

            context.assets

                .open(
                    KnowledgeAssets.RUNTIME_ROOT +
                            "nutrition_alias.json"
                )

                .bufferedReader()

                .use {

                    it.readText()

                }

        validateNoDuplicateKeys(
            json
        )

        val type =

            object :
                TypeToken<Map<String, String>>() {}.type

        return gson.fromJson<Map<String, String>>(

            json,

            type

        )
            .toSortedMap()
    }

    private fun validateNoDuplicateKeys(
        json: String
    ) {

        val keys =

            Regex(
                """"([^"]+)"\s*:"""
            )
                .findAll(
                    json
                )
                .map {
                    it.groupValues[1]
                }
                .toList()

        val duplicateKeys =

            keys
                .groupingBy {
                    it
                }
                .eachCount()
                .filterValues { count ->

                    count > 1

                }
                .keys
                .sorted()

        require(
            duplicateKeys.isEmpty()
        ) {

            "Duplicate nutrition alias keys found: $duplicateKeys"

        }
    }

}