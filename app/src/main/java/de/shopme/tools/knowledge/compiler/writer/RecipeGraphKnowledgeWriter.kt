package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.recipegraph.RecipeGraphEntry
import de.shopme.tools.knowledge.recipegraph.RecipeGraphKnowledge
import de.shopme.tools.knowledge.recipegraph.RecipeGraphKnowledgeJsonWriter
import java.io.File

class RecipeGraphKnowledgeWriter :

    AbstractKnowledgeWriter<String, RecipeGraphEntry>() {

    override fun key(

        context: CompilerContext

    ) =

        context.nutritionReference

    override fun value(

        context: CompilerContext

    ) =

        context.recipeGraph

    fun knowledge() =

        RecipeGraphKnowledge(

            entries =

                entries.toSortedMap()

        )

    override fun finish() {

        RecipeGraphKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "data/generated/recipe_graph.json"

                )

            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 RECIPE GRAPH KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${entries.size}")
        println("📄 Output  : data/generated/recipe_graph.json")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

}