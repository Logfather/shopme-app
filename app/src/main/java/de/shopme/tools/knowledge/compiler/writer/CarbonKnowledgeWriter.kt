package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.carbon.CarbonFootprint
import de.shopme.tools.knowledge.carbon.CarbonKnowledge
import de.shopme.tools.knowledge.carbon.CarbonKnowledgeJsonWriter
import de.shopme.tools.knowledge.compiler.CompilerContext
import java.io.File

class CarbonKnowledgeWriter :

    AbstractKnowledgeWriter<String, CarbonFootprint>() {

    override fun key(

        context: CompilerContext

    ) =

        context.nutritionReference

    override fun value(

        context: CompilerContext

    ) =

        context.carbonFootprint

    fun knowledge() =

        CarbonKnowledge(

            entries =

                entries.toSortedMap()

        )

    override fun finish() {

        CarbonKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "build/generated/carbon_footprint.json"

                )

            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 CARBON KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${entries.size}")
        println("📄 Output  : build/generated/carbon_footprint.json")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

}