package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.carbon.CarbonFootprint
import de.shopme.tools.knowledge.carbon.CarbonKnowledge
import de.shopme.tools.knowledge.carbon.CarbonKnowledgeJsonWriter
import de.shopme.tools.knowledge.carbon.builder.CarbonKnowledgeBuilder
import de.shopme.tools.knowledge.compiler.CompilerContext
import java.io.File

class CarbonKnowledgeWriter(

    private val builder: CarbonKnowledgeBuilder? = null,

    private val outputFile: File =
        File(
            "build/generated/carbon_footprint.json"
        ),

    private val runtimeOutputFile: File =
        File(
            "src/main/assets/knowledge/runtime/carbon_footprint.json"
        )

) : AbstractKnowledgeWriter<String, CarbonFootprint>() {

    override fun key(

        context: CompilerContext

    ) =

        context.nutritionReference

    override fun value(

        context: CompilerContext

    ) =

        context.carbonFootprint

    fun putAll(

        carbon: Map<String, CarbonFootprint>

    ) {

        entries.putAll(

            carbon

        )
    }

    fun knowledge() =

        CarbonKnowledge(

            entries =

                entries.toSortedMap()

        )

    override fun finish() {

        builder
            ?.build()
            ?.let { builtCarbon ->

                putAll(
                    builtCarbon
                )
            }

        val knowledge =
            knowledge()

        CarbonKnowledgeJsonWriter()
            .write(
                knowledge,
                outputFile
            )

        CarbonKnowledgeJsonWriter()
            .write(
                knowledge,
                runtimeOutputFile
            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 CARBON KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${entries.size}")
        println("📄 Output  : ${outputFile.path}")
        println("📄 Runtime : ${runtimeOutputFile.path}")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()
    }
}