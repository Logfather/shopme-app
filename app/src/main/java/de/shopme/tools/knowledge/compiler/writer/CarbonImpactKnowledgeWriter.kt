package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.carbon.CarbonImpactKnowledge
import de.shopme.tools.knowledge.carbon.CarbonImpactKnowledgeJsonWriter
import de.shopme.tools.knowledge.carbon.CarbonImpactLevel
import de.shopme.tools.knowledge.compiler.CompilerContext
import java.io.File

class CarbonImpactKnowledgeWriter :

    AbstractKnowledgeWriter<String, CarbonImpactLevel>() {

    override fun key(

        context: CompilerContext

    ) =

        context.nutritionReference

    override fun value(
        context: CompilerContext
    ) =
        context.carbonImpact

    fun knowledge() =

        CarbonImpactKnowledge(

            entries =

                entries.toSortedMap()

        )

    override fun finish() {

        CarbonImpactKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "data/generated/carbon_impact.json"

                )

            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 CARBON IMPACT KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${entries.size}")
        println("📄 Output  : data/generated/carbon_impact.json")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

}