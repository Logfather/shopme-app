package de.shopme.tools.knowledge.pesticides

import com.google.gson.GsonBuilder
import de.shopme.tools.knowledge.pesticide.PesticideKnowledge
import java.io.File

class PesticidesKnowledgeJsonWriter {

    private val gson =

        GsonBuilder()

            .setPrettyPrinting()

            .create()

    fun write(

        knowledge: PesticideKnowledge,

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