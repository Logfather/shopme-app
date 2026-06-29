package de.shopme.tools.knowledge.foods.loader

import com.google.gson.Gson
import de.shopme.tools.knowledge.foods.FoodsKnowledge
import java.io.File

class FileFoodsKnowledgeLoader(

    private val file: File,

    private val failIfMissing: Boolean = false

) : FoodsKnowledgeLoader {

    private val gson = Gson()

    override fun load(): FoodsKnowledge {

        if (!file.exists()) {

            if (failIfMissing) {

                error("Foods knowledge file not found: ${file.path}")

            }

            return FoodsKnowledge(
                version = 1,
                foods = emptyList()
            )
        }

        return gson.fromJson(

            file.readText(),

            FoodsKnowledge::class.java

        )
    }

}