package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.ingredientgraph.IngredientGraphEntry
import de.shopme.tools.knowledge.ingredientgraph.IngredientGraphKnowledge
import de.shopme.tools.knowledge.ingredientgraph.IngredientGraphKnowledgeJsonWriter
import java.io.File

class IngredientGraphKnowledgeWriter :

    AbstractKnowledgeWriter<String, IngredientGraphEntry>() {

    override fun key(

        context: CompilerContext

    ) =

        context.nutritionReference

    override fun value(

        context: CompilerContext

    ) =

        context.ingredientGraph

    fun knowledge() =

        IngredientGraphKnowledge(

            entries =

                entries.toSortedMap()

        )

    override fun finish() {

        IngredientGraphKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "build/generated/ingredient_graph.json"

                )

            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 INGREDIENT GRAPH KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${entries.size}")
        println("📄 Output  : build/generated/ingredient_graph.json")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

}