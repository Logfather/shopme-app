package de.shopme.tools.knowledge.semantics

import com.google.gson.GsonBuilder
import java.io.File

class FoodSemanticsKnowledgeJsonWriter {

    private val gson =

        GsonBuilder()

            .setPrettyPrinting()

            .create()

    fun write(

        knowledge: FoodSemanticsKnowledge,

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