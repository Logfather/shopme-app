package de.shopme.tools.knowledge.category

import android.content.Context
import com.google.gson.Gson

class CategoryKnowledgeLoader(

    private val context: Context

) {

    companion object {

        private const val FILE_NAME =
            "knowledge/food_taxonomy.json"

    }

    fun load(): CategoryKnowledge {

        val json = context.assets
            .open(FILE_NAME)
            .bufferedReader()
            .use { it.readText() }

        return Gson().fromJson(
            json,
            CategoryKnowledge::class.java
        )

    }

}