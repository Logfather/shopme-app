package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.foodmiles.FoodMiles
import de.shopme.tools.knowledge.foodmiles.FoodMilesKnowledge
import de.shopme.tools.knowledge.foodmiles.FoodMilesKnowledgeJsonWriter
import java.io.File

class FoodMilesKnowledgeWriter :

    AbstractKnowledgeWriter<String, FoodMiles>() {

    override fun key(

        context: CompilerContext

    ) =

        context.nutritionReference

    override fun value(

        context: CompilerContext

    ) =

        context.foodMiles

    fun knowledge() =

        FoodMilesKnowledge(

            entries =

                entries.toSortedMap()

        )

    override fun finish() {

        FoodMilesKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "data/generated/food_miles.json"

                )

            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 FOOD MILES KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${entries.size}")
        println("📄 Output  : data/generated/food_miles.json")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

}