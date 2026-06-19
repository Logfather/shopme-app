package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.semantics.FoodSemanticsEntry
import de.shopme.tools.knowledge.semantics.FoodSemanticsKnowledge
import de.shopme.tools.knowledge.semantics.FoodSemanticsKnowledgeJsonWriter
import java.io.File

class FoodSemanticsKnowledgeWriter :

    AbstractKnowledgeWriter<String, FoodSemanticsEntry>() {

    override fun key(

        context: CompilerContext

    ) =

        context.foodTaxonomy?.parent

    override fun value(

        context: CompilerContext

    ) =

        if (

            context.foodCategory.name == "UNKNOWN" &&

            context.tags.isEmpty()

        ) {

            null

        } else {

            FoodSemanticsEntry(

                category =

                    context.foodCategory,

                tags =

                    context.tags.toSet()

            )

        }

    fun knowledge() =

        FoodSemanticsKnowledge(

            entries =

                entries.toSortedMap()

        )

    override fun finish() {

        FoodSemanticsKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "build/generated/food_semantics.json"

                )

            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 FOOD SEMANTICS KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${entries.size}")
        println("📄 Output  : build/generated/food_semantics.json")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

}