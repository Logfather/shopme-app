package de.shopme.tools.knowledge.recipegraph

import com.google.gson.GsonBuilder
import java.io.File

class RecipeGraphKnowledgeJsonWriter {

    private val gson =

        GsonBuilder()

            .setPrettyPrinting()

            .create()

    fun write(

        knowledge: RecipeGraphKnowledge,

        output: File

    ) {

        output.parentFile?.mkdirs()

        output.writeText(

            gson.toJson(

                knowledge

            )

        )

    }

}