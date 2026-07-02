package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.recipe.RecipeKnowledge
import de.shopme.tools.knowledge.recipe.RecipeKnowledgeJsonWriter
import java.io.File

class RecipeKnowledgeWriter :

    AbstractKnowledgeWriter<String, List<String>>() {

    override fun key(
        context: CompilerContext
    ) =
        context.nutritionReference

    override fun value(
        context: CompilerContext
    ): List<String>? {

        return context.recipes

            .takeIf {
                it.isNotEmpty()
            }

            ?.sorted()

    }

    fun knowledge() =

        RecipeKnowledge(

            entries =

                entries.toSortedMap()

        )

    override fun finish() {

        RecipeKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "data/generated/recipes.json"

                )

            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 RECIPE KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${entries.size}")
        println("📄 Output  : data/generated/recipes.json")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

}