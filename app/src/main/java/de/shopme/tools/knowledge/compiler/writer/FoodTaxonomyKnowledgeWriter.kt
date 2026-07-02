package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.taxonomy.FoodTaxonomyEntry
import de.shopme.tools.knowledge.taxonomy.FoodTaxonomyKnowledge
import de.shopme.tools.knowledge.taxonomy.FoodTaxonomyKnowledgeJsonWriter
import java.io.File

class FoodTaxonomyKnowledgeWriter :

    AbstractKnowledgeWriter<String, FoodTaxonomyEntry>() {

    override fun key(

        context: CompilerContext

    ) =

        context.nutritionReference

    override fun value(

        context: CompilerContext

    ) =

        context.foodTaxonomy

    fun knowledge() =

        FoodTaxonomyKnowledge(

            entries =

                entries.toSortedMap()

        )

    override fun finish() {

        FoodTaxonomyKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "data/generated/food_taxonomy.json"

                )

            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 FOOD TAXONOMY KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${entries.size}")
        println("📄 Output  : data/generated/food_taxonomy.json")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

}