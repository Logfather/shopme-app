package de.shopme.tools.knowledge.seasonality

import com.google.gson.GsonBuilder
import java.io.File

class SeasonalityKnowledgeJsonWriter {

    private val gson =

        GsonBuilder()

            .setPrettyPrinting()

            .create()

    fun write(

        knowledge: SeasonalityKnowledge,

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