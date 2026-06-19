package de.shopme.tools.knowledge.ingredients

import com.google.gson.GsonBuilder
import java.io.File

class IngredientsKnowledgeJsonWriter {

    private val gson =

        GsonBuilder()

            .setPrettyPrinting()

            .create()

    fun write(

        knowledge: IngredientsKnowledge,

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