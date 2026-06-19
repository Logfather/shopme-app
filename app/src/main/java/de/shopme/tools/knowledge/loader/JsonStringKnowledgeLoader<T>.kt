package de.shopme.tools.knowledge.loader

import com.google.gson.Gson
import java.lang.reflect.Type

abstract class JsonStringKnowledgeLoader<T>(

    private val json: String,

    private val type: Type,

    private val gson: Gson = Gson()

) : KnowledgeAssetLoader<T> {

    @Suppress("UNCHECKED_CAST")

    override fun load(): T {

        return gson.fromJson(

            json,

            type

        )

    }

}