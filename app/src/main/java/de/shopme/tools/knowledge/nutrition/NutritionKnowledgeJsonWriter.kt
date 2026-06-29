package de.shopme.tools.knowledge.nutrition

import com.google.gson.GsonBuilder
import java.io.File

class NutritionKnowledgeJsonWriter {

    private val gson =

        GsonBuilder()

            .setPrettyPrinting()

            .create()

    fun write(

        knowledge: NutritionFactsKnowledge,

        output: File

    ) {

        output
            .parentFile
            ?.mkdirs()

        val sortedKnowledge =

            NutritionFactsKnowledge(

                entries =

                    knowledge
                        .entries
                        .toSortedMap()

            )

        output.writeText(

            gson.toJson(

                sortedKnowledge

            )

        )

    }

}