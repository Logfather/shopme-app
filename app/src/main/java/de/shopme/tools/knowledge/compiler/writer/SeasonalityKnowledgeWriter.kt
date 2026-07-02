package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.seasonality.SeasonalityKnowledge
import de.shopme.tools.knowledge.seasonality.SeasonalityKnowledgeJsonWriter
import java.io.File

class SeasonalityKnowledgeWriter :

    AbstractKnowledgeWriter<String, List<Int>>() {

    override fun key(

        context: CompilerContext

    ) =

        context.nutritionReference

    override fun value(

        context: CompilerContext

    ) =

        context.seasonality

    fun knowledge() =

        SeasonalityKnowledge(

            entries =

                entries.toSortedMap()

        )

    override fun finish() {

        SeasonalityKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "data/generated/seasonality.json"

                )

            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 SEASONALITY KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${entries.size}")
        println("📄 Output  : data/generated/seasonality.json")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

}