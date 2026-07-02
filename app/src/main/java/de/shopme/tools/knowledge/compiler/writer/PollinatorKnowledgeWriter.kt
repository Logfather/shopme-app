package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.pollinator.PollinatorKnowledge
import de.shopme.tools.knowledge.pollinator.PollinatorKnowledgeJsonWriter
import de.shopme.tools.knowledge.pollinator.PollinatorScore
import java.io.File

class PollinatorKnowledgeWriter :

    AbstractKnowledgeWriter<String, PollinatorScore>() {

    override fun key(

        context: CompilerContext

    ) =

        context.nutritionReference

    override fun value(

        context: CompilerContext

    ) =

        context.pollinator

    fun knowledge() =

        PollinatorKnowledge(

            entries =

                entries.toSortedMap()

        )

    override fun finish() {

        PollinatorKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "data/generated/pollinator.json"

                )

            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 POLLINATOR KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${entries.size}")
        println("📄 Output  : data/generated/pollinator.json")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

}