package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.production.ProductionKnowledge
import de.shopme.tools.knowledge.production.ProductionKnowledgeJsonWriter
import de.shopme.tools.knowledge.production.ProductionMethod
import java.io.File

class ProductionKnowledgeWriter :

    AbstractKnowledgeWriter<String, Set<ProductionMethod>>() {

    override fun key(

        context: CompilerContext

    ) =

        context.nutritionReference

    override fun value(

        context: CompilerContext

    ) =

        context.production

            .toSortedSet()

            .takeIf {

                it.isNotEmpty()

            }

    fun knowledge() =

        ProductionKnowledge(

            entries =

                entries.toSortedMap()

        )

    override fun finish() {

        ProductionKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "build/generated/production.json"

                )

            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 PRODUCTION KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${entries.size}")
        println("📄 Output  : build/generated/production.json")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

}