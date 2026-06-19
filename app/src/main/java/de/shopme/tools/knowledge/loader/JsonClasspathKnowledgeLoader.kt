package de.shopme.tools.knowledge.loader

import com.google.gson.Gson

abstract class JsonClasspathKnowledgeLoader<T>(

    private val assetName: String,

    private val clazz: Class<T>

) : KnowledgeAssetLoader<T> {

    private val gson = Gson()

    override fun load(): T {

        val json =

            checkNotNull(

                javaClass.classLoader
                    ?.getResourceAsStream(assetName)

            )
                .bufferedReader()
                .use { it.readText() }

        return gson.fromJson(
            json,
            clazz
        )
    }

}