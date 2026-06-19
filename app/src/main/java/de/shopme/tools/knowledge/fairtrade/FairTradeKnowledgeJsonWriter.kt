package de.shopme.tools.knowledge.fairtrade

import com.google.gson.GsonBuilder
import java.io.File

class FairTradeKnowledgeJsonWriter {

    private val gson =

        GsonBuilder()

            .setPrettyPrinting()

            .create()

    fun write(

        knowledge: FairTradeKnowledge,

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