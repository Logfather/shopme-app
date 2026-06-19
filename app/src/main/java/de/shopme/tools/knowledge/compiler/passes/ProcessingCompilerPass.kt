package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.processing.ProcessingResolver

class ProcessingCompilerPass(

    private val resolver:

    ProcessingResolver

) : FoodKnowledgeCompilerPass {

    override fun process(

        context: CompilerContext

    ) {

        val result =

            resolver.resolve(

                context.nutritionReference

            )

        context.processing =

            result

    }

}