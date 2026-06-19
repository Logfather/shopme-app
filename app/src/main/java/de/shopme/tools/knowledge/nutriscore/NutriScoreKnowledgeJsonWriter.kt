package de.shopme.tools.knowledge.nutriscore

import com.google.gson.GsonBuilder
import java.io.File

class NutriScoreKnowledgeJsonWriter {

    private val gson =

        GsonBuilder()

            .setPrettyPrinting()

            .create()

    fun write(

        knowledge: NutriScoreFactsKnowledge,

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