package de.shopme.tools.knowledge.loader

import android.content.Context
import com.google.gson.Gson

abstract class JsonKnowledgeLoader<T>(

    private val context: Context,

    private val assetName: String,

    private val clazz: Class<T>

) : KnowledgeAssetLoader<T> {

    private val gson =
        Gson()

    override fun load(): T {

        val json =

            context.assets

                .open(assetName)

                .bufferedReader()

                .use {

                    it.readText()

                }

        return gson.fromJson(

            json,

            clazz

        )

    }

}