package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.allergen.Allergen
import de.shopme.tools.knowledge.allergen.AllergenKnowledge
import de.shopme.tools.knowledge.allergen.AllergenKnowledgeJsonWriter
import de.shopme.tools.knowledge.compiler.CompilerContext
import java.io.File

class AllergenKnowledgeWriter :

    AbstractKnowledgeWriter<String, Set<Allergen>>() {

    override fun key(

        context: CompilerContext

    ) =

        context.nutritionReference

    override fun value(

        context: CompilerContext

    ) =

        context.allergens

            .toSortedSet()

            .takeIf {

                it.isNotEmpty()

            }

    fun knowledge() =

        AllergenKnowledge(

            entries =

                entries.toSortedMap()

        )

    override fun finish() {

        AllergenKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "build/generated/allergens.json"

                )

            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 ALLERGEN KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${entries.size}")
        println("📄 Output  : build/generated/allergens.json")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

}