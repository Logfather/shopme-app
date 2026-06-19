package de.shopme.tools.knowledge.glycemic

import com.google.gson.GsonBuilder
import java.io.File

class GlycemicKnowledgeJsonWriter {

    private val gson =

        GsonBuilder()

            .setPrettyPrinting()

            .create()

    fun write(

        knowledge: GlycemicKnowledge,

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