package de.shopme.tools.knowledge.compiler

import de.shopme.tools.knowledge.publisher.RuntimeKnowledgePublisher
import de.shopme.tools.knowledge.reader.ResourceCatalogReader
import java.io.File

object CreateFoodKnowledge {

    @JvmStatic
    fun main(args: Array<String>) {

        val reader =

            ResourceCatalogReader()

        val compiler =

            DefaultFoodKnowledgeBuildCompiler.create()


        val catalog =

            reader.read()

        val knowledge =

            catalog

                .map(

                    compiler::compile

                )

                .sortedBy {

                    it.normalizedName

                }

        RuntimeKnowledgePublisher(

            generatedDirectory =

                File(

                    "build/generated"

                ),

            runtimeDirectory =

                File(

                    "build/runtimeKnowledge"

                )

        ).publish()


        println()

        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 FOOD KNOWLEDGE BUILD")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${knowledge.size}")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

}