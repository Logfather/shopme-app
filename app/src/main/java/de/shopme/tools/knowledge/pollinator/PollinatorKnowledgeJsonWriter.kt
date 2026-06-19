package de.shopme.tools.knowledge.pollinator

import com.google.gson.GsonBuilder
import java.io.File

class PollinatorKnowledgeJsonWriter {

    private val gson =

        GsonBuilder()

            .setPrettyPrinting()

            .create()

    fun write(

        knowledge: PollinatorKnowledge,

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