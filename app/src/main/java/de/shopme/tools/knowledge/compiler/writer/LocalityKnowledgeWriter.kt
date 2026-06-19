package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.locality.Locality
import de.shopme.tools.knowledge.locality.LocalityKnowledge
import de.shopme.tools.knowledge.locality.LocalityKnowledgeJsonWriter
import java.io.File

class LocalityKnowledgeWriter :

    AbstractKnowledgeWriter<String, Locality>() {

    override fun key(

        context: CompilerContext

    ) =

        context.nutritionReference

    override fun value(

        context: CompilerContext

    ) =

        context.locality

    fun knowledge() =

        LocalityKnowledge(

            entries =

                entries.toSortedMap()

        )

    override fun finish() {

        LocalityKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "build/generated/locality.json"

                )

            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 LOCALITY KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${entries.size}")
        println("📄 Output  : build/generated/locality.json")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

}