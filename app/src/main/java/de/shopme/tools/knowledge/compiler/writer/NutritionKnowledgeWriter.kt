package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.nutrition.NutritionFacts
import de.shopme.tools.knowledge.nutrition.NutritionFactsKnowledge
import de.shopme.tools.knowledge.nutrition.NutritionKnowledgeJsonWriter
import java.io.File

class NutritionKnowledgeWriter :
    AbstractKnowledgeWriter<String, NutritionFacts>() {

    override fun key(
        context: CompilerContext
    ): String? {

        return context.nutritionReference

    }

    override fun value(
        context: CompilerContext
    ): NutritionFacts? {

        return context.nutritionFacts

    }

    fun knowledge(): NutritionFactsKnowledge {

        return NutritionFactsKnowledge(

            entries = entries.toSortedMap()

        )

    }

    override fun finish() {

        NutritionKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "build/generated/nutrition.json"

                )

            )

    }

}