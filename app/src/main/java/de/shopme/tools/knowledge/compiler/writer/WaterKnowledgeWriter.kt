package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.waterfootprint.WaterFootprint
import de.shopme.tools.knowledge.waterfootprint.WaterKnowledge
import de.shopme.tools.knowledge.waterfootprint.WaterKnowledgeJsonWriter
import java.io.File

class WaterKnowledgeWriter :

    AbstractKnowledgeWriter<String, WaterFootprint>() {

    override fun key(

        context: CompilerContext

    ) =

        context.nutritionReference

    override fun value(

        context: CompilerContext

    ) =

        context.waterFootprint

    fun knowledge() =

        WaterKnowledge(

            entries =

                entries.toSortedMap()

        )

    override fun finish() {

        WaterKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "build/generated/water_footprint.json"

                )

            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 WATER KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${entries.size}")
        println("📄 Output  : build/generated/water_footprint.json")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

}