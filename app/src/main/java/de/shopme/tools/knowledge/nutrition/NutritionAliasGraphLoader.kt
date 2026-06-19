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
                    KnowledgeAssets.ROOT +
                            "nutrition_alias.json"
                )

                .bufferedReader()

                .use {

                    it.readText()

                }

        val type =

            object :
                TypeToken<Map<String, String>>() {}.type

        return gson.fromJson(

            json,

            type

        )

    }

}