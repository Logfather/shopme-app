package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.ingredients.IngredientsKnowledge
import de.shopme.tools.knowledge.ingredients.IngredientsKnowledgeJsonWriter
import java.io.File

class IngredientsKnowledgeWriter :

    AbstractKnowledgeWriter<String, Set<String>>() {

    override fun key(

        context: CompilerContext

    ) =

        context.nutritionReference

    override fun value(

        context: CompilerContext

    ) =

        context.ingredients

    fun knowledge() =

        IngredientsKnowledge(

            entries =

                entries.toSortedMap()

        )

    override fun finish() {

        IngredientsKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "build/generated/ingredients.json"

                )

            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 INGREDIENTS KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${entries.size}")
        println("📄 Output  : build/generated/ingredients.json")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

}