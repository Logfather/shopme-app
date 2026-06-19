package de.shopme.tools.knowledge.foodmiles

import com.google.gson.GsonBuilder
import java.io.File

class FoodMilesKnowledgeJsonWriter {

    private val gson =

        GsonBuilder()

            .setPrettyPrinting()

            .create()

    fun write(

        knowledge: FoodMilesKnowledge,

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