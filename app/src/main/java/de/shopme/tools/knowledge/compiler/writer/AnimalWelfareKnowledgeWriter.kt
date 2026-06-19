package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.animalwelfare.AnimalWelfare
import de.shopme.tools.knowledge.animalwelfare.AnimalWelfareKnowledge
import de.shopme.tools.knowledge.animalwelfare.AnimalWelfareKnowledgeJsonWriter
import de.shopme.tools.knowledge.compiler.CompilerContext
import java.io.File

class AnimalWelfareKnowledgeWriter :

    AbstractKnowledgeWriter<String, AnimalWelfare>() {

    override fun key(

        context: CompilerContext

    ) =

        context.nutritionReference

    override fun value(

        context: CompilerContext

    ) =

        context.animalWelfare

    fun knowledge() =

        AnimalWelfareKnowledge(

            entries =

                entries.toSortedMap()

        )

    override fun finish() {

        AnimalWelfareKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "build/generated/animal_welfare.json"

                )

            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 ANIMAL WELFARE KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${entries.size}")
        println("📄 Output  : build/generated/animal_welfare.json")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

}