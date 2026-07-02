package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.pesticide.Pesticide
import de.shopme.tools.knowledge.pesticide.PesticideKnowledge
import de.shopme.tools.knowledge.pesticides.PesticidesKnowledgeJsonWriter
import java.io.File

class PesticidesKnowledgeWriter :

    AbstractKnowledgeWriter<String, Pesticide>() {

    override fun key(
        context: CompilerContext
    ) =
        context.nutritionReference

    override fun value(
        context: CompilerContext
    ) =
        context.pesticide

    fun knowledge() =

        PesticideKnowledge(

            entries =

                entries.toSortedMap()

        )

    override fun finish() {

        PesticidesKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "data/generated/pesticides.json"

                )

            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 PESTICIDES KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${entries.size}")
        println("📄 Output  : data/generated/pesticides.json")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

}