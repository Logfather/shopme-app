package de.shopme.tools.knowledge.animalwelfare

import com.google.gson.GsonBuilder
import java.io.File

class AnimalWelfareKnowledgeJsonWriter {

    private val gson =

        GsonBuilder()

            .setPrettyPrinting()

            .create()

    fun write(

        knowledge: AnimalWelfareKnowledge,

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