package de.shopme.tools.knowledge.ingredientgraph

import com.google.gson.GsonBuilder
import java.io.File

class IngredientGraphKnowledgeJsonWriter {

    private val gson =

        GsonBuilder()

            .setPrettyPrinting()

            .create()

    fun write(

        knowledge: IngredientGraphKnowledge,

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