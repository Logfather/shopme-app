package de.shopme.tools.knowledge.taxonomy

import android.content.Context
import com.google.gson.Gson
import de.shopme.tools.knowledge.KnowledgeAssets

class FoodTaxonomyGraphLoader(

    private val context: Context

) {

    private val gson =

        Gson()

    fun load(): FoodTaxonomyKnowledge {

        val json =

            context.assets

                .open(

                    KnowledgeAssets.RUNTIME_ROOT + "food_taxonomy.json")

                .bufferedReader()

                .use {

                    it.readText()

                }

        return gson.fromJson(

            json,

            FoodTaxonomyKnowledge::class.java

        )

    }

}