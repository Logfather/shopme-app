package de.shopme.tools.knowledge.compiler

import de.shopme.tools.knowledge.FoodKnowledgeAssembler

object DefaultFoodKnowledgeBuildCompiler {

    fun create(
        pipeline: FoodKnowledgeCompilerPipeline
    ): FoodKnowledgeCompiler {

        return FoodKnowledgeCompiler(

            pipeline = pipeline,

            assembler = FoodKnowledgeAssembler()

        )
    }
}