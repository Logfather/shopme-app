package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.packaging.Packaging
import de.shopme.tools.knowledge.packaging.PackagingKnowledge
import de.shopme.tools.knowledge.packaging.PackagingKnowledgeJsonWriter
import java.io.File

class PackagingKnowledgeWriter :

    AbstractKnowledgeWriter<String, Packaging>() {

    override fun key(

        context: CompilerContext

    ) =

        context.nutritionReference

    override fun value(

        context: CompilerContext

    ) =

        context.packaging

    fun knowledge() =

        PackagingKnowledge(

            entries =

                entries.toSortedMap()

        )

    override fun finish() {

        PackagingKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "build/generated/packaging.json"

                )

            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 PACKAGING KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${entries.size}")
        println("📄 Output  : build/generated/packaging.json")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

}