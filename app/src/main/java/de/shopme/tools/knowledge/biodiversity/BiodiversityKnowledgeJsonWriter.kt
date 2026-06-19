package de.shopme.tools.knowledge.biodiversity

import com.google.gson.GsonBuilder
import java.io.File

class BiodiversityKnowledgeJsonWriter {

    private val gson =

        GsonBuilder()

            .setPrettyPrinting()

            .create()

    fun write(

        knowledge: BiodiversityKnowledge,

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