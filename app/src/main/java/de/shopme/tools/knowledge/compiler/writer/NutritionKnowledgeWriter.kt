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

        val deduplicatedEntries =
            entries
                .filterKeys { key ->

                    key.isNotBlank()

                }
                .toSortedMap()

        return NutritionFactsKnowledge(

            entries = deduplicatedEntries

        )

    }

    override fun finish() {

        val knowledge =
            knowledge()

        NutritionKnowledgeJsonWriter()

            .write(

                knowledge,

                File(

                    "build/generated/nutrition.json"

                )

            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 NUTRITION KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${knowledge.entries.size}")
        println("📄 Output  : build/generated/nutrition.json")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()
    }
}