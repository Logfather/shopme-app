package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.biodiversity.BiodiversityKnowledge
import de.shopme.tools.knowledge.biodiversity.BiodiversityKnowledgeJsonWriter
import de.shopme.tools.knowledge.biodiversity.BiodiversityScore
import de.shopme.tools.knowledge.compiler.CompilerContext
import java.io.File

class BiodiversityKnowledgeWriter :

    AbstractKnowledgeWriter<String, BiodiversityScore>() {

    override fun key(

        context: CompilerContext

    ) =

        context.nutritionReference

    override fun value(

        context: CompilerContext

    ) =

        context.biodiversity

    fun knowledge() =

        BiodiversityKnowledge(

            entries =

                entries.toSortedMap()

        )

    override fun finish() {

        BiodiversityKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "data/generated/biodiversity.json"

                )

            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 BIODIVERSITY KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${entries.size}")
        println("📄 Output  : data/generated/biodiversity.json")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

}