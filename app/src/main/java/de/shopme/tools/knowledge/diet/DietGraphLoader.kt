package de.shopme.tools.knowledge.diet

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.KnowledgeAssets

class DietGraphLoader(

    private val context: Context

) {

    private val gson = Gson()

    fun load(): DietKnowledge {

        val json =

            context.assets

                .open(
                    KnowledgeAssets.RUNTIME_ROOT +
                            "diet_classification.json"
                )

                .bufferedReader()

                .use {

                    it.readText()

                }

        val type =

            object :
                TypeToken<Map<String, Set<DietClassification>>>() {}.type

        return gson.fromJson(

            json,

            type

        )

    }

}