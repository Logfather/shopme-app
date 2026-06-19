package de.shopme.tools.knowledge.taxonomy

import com.google.gson.GsonBuilder
import java.io.File

class FoodTaxonomyKnowledgeJsonWriter {

    private val gson =

        GsonBuilder()

            .setPrettyPrinting()

            .create()

    fun write(

        knowledge: FoodTaxonomyKnowledge,

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