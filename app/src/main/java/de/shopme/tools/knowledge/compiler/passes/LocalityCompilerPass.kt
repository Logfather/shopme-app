package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.locality.LocalityResolver

class LocalityCompilerPass(

    private val resolver:

    LocalityResolver

) : FoodKnowledgeCompilerPass {

    override fun process(

        context: CompilerContext

    ) {

        val result =

            resolver.resolve(

                context.nutritionReference

            )

        context.locality =

            result

    }

}