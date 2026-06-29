package de.shopme.tools.knowledge.compiler

import java.io.File

object CreateFoodKnowledge {

    @JvmStatic
    fun main(args: Array<String>) {

        val importFile =
            args
                .firstOrNull()
                ?.let(::File)

        build(importFile)
    }

    fun build(
        importFile: File? = null
    ) {
        FoodKnowledgeBuildCompiler()
            .build(importFile)
    }
}