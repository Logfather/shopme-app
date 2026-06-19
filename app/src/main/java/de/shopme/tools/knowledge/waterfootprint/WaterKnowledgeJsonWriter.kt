package de.shopme.tools.knowledge.waterfootprint

import com.google.gson.GsonBuilder
import java.io.File

class WaterKnowledgeJsonWriter {

    private val gson =

        GsonBuilder()

            .setPrettyPrinting()

            .create()

    fun write(

        knowledge: WaterKnowledge,

        output: File

    ) {

        output.parentFile.mkdirs()

        output.writeText(

            gson.toJson(

                knowledge

            )

        )

    }

}