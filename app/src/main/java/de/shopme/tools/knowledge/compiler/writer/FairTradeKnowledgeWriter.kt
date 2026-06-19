package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.fairtrade.FairTrade
import de.shopme.tools.knowledge.fairtrade.FairTradeKnowledge
import de.shopme.tools.knowledge.fairtrade.FairTradeKnowledgeJsonWriter
import java.io.File

class FairTradeKnowledgeWriter :

    AbstractKnowledgeWriter<String, FairTrade>() {

    override fun key(

        context: CompilerContext

    ) =

        context.nutritionReference

    override fun value(

        context: CompilerContext

    ) =

        context.fairTrade

    fun knowledge() =

        FairTradeKnowledge(

            entries =

                entries.toSortedMap()

        )

    override fun finish() {

        FairTradeKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "build/generated/fairtrade.json"

                )

            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 FAIRTRADE KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${entries.size}")
        println("📄 Output  : build/generated/fairtrade.json")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

}