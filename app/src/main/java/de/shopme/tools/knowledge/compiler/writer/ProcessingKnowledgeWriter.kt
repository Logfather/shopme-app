package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.processing.ProcessingKnowledge
import de.shopme.tools.knowledge.processing.ProcessingKnowledgeJsonWriter
import de.shopme.tools.knowledge.processing.ProcessingLevel
import java.io.File

class ProcessingKnowledgeWriter :

    AbstractKnowledgeWriter<String, ProcessingLevel>() {

    override fun key(
        context: CompilerContext
    ) =
        context.nutritionReference

    override fun value(
        context: CompilerContext
    ) =
        context.processing

    fun knowledge() =

        ProcessingKnowledge(

            entries =

                entries.toSortedMap()

        )

    override fun finish() {

        ProcessingKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "data/generated/processing.json"

                )

            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 PROCESSING KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${entries.size}")
        println("📄 Output  : data/generated/processing.json")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

}