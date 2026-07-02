package de.shopme.tools.knowledge.compiler.writer

import android.R.attr.entries
import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.waterstress.WaterStress
import de.shopme.tools.knowledge.waterstress.WaterStressKnowledge
import de.shopme.tools.knowledge.waterstress.WaterStressKnowledgeJsonWriter
import java.io.File

class WaterStressKnowledgeWriter :

    AbstractKnowledgeWriter<String, WaterStress>() {

    override fun key(

        context: CompilerContext

    ) =

        context.nutritionReference

    override fun value(

        context: CompilerContext

    ) =

        context.waterStress

    fun knowledge() =

        WaterStressKnowledge(

            entries =

                entries.toSortedMap()

        )



    override fun finish() {

        WaterStressKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "data/generated/water_stress.json"

                )

            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 WATER STRESS KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${entries.size}")
        println("📄 Output  : data/generated/water_stress.json")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

}