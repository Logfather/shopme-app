package de.shopme.tools.knowledge.carbon

import com.google.gson.GsonBuilder
import java.io.File

class CarbonImpactKnowledgeJsonWriter {

    private val gson =

        GsonBuilder()

            .setPrettyPrinting()

            .create()

    fun write(

        knowledge: CarbonImpactKnowledge,

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