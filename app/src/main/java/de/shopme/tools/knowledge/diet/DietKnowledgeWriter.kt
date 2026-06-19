package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.diet.DietClassification
import de.shopme.tools.knowledge.diet.DietKnowledge
import de.shopme.tools.knowledge.diet.DietKnowledgeJsonWriter
import java.io.File

class DietKnowledgeWriter :

    AbstractKnowledgeWriter<String, Set<DietClassification>>() {

    override fun key(

        context: CompilerContext

    ) =

        context.nutritionReference

    override fun value(

        context: CompilerContext

    ) =

        context.dietClassifications

            .toSortedSet()

            .takeIf {

                it.isNotEmpty()

            }

    fun knowledge() =

        DietKnowledge(

            entries =

                entries.toSortedMap()

        )

    override fun finish() {

        DietKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "build/generated/diet_classification.json"

                )

            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 DIET KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${entries.size}")
        println("📄 Output  : build/generated/diet_classification.json")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

}