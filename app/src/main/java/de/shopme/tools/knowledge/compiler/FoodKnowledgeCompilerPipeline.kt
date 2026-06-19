package de.shopme.tools.knowledge.compiler

import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass

class FoodKnowledgeCompilerPipeline(

    private val passes: List<FoodKnowledgeCompilerPass>

) {

    fun process(
        context: CompilerContext
    ) {

        passes.forEach {

            it.process(context)

        }

    }

}