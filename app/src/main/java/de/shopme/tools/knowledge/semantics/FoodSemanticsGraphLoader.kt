package de.shopme.tools.knowledge.semantics

import android.content.Context
import com.google.gson.Gson
import de.shopme.tools.knowledge.KnowledgeAssets

class FoodSemanticsGraphLoader(

    private val context: Context

) {

    private val gson =

        Gson()

    fun load(): FoodSemanticsKnowledge {

        val json =

            context.assets

                .open(
                    KnowledgeAssets.ROOT +
                            "food_semantics.json"
                )

                .bufferedReader()

                .use {

                    it.readText()

                }

        return gson.fromJson(

            json,

            FoodSemanticsKnowledge::class.java

        )

    }

}