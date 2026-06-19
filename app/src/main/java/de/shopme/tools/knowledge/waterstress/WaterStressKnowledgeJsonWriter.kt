package de.shopme.tools.knowledge.waterstress

import com.google.gson.GsonBuilder
import java.io.File

class WaterStressKnowledgeJsonWriter {

    private val gson =

        GsonBuilder()

            .setPrettyPrinting()

            .create()

    fun write(

        knowledge: WaterStressKnowledge,

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