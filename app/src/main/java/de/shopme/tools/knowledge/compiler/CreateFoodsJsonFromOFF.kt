package de.shopme.tools.knowledge.compiler

import de.shopme.tools.knowledge.ai.builder.DefaultAIKnowledgeBuilderPipelineFactory
import de.shopme.tools.knowledge.ai.builder.off.OFFBuildRequestFactory
import java.io.File

object CreateFoodsJsonFromOFF {

    @JvmStatic
    fun main(args: Array<String>) {

        val outputFile =
            if (args.isNotEmpty()) {
                File(args[0])
            } else {
                File("data/generated/foods.off.json")
            }

        outputFile.parentFile.mkdirs()

        val pipeline =
            DefaultAIKnowledgeBuilderPipelineFactory.create(
                catalogFile = outputFile
            )

        val request =
            OFFBuildRequestFactory.create()

        val result =
            pipeline.run(request)

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OFF BUILD")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Candidates : ${result.candidates.size}")
        println("Output     : ${outputFile.absolutePath}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}