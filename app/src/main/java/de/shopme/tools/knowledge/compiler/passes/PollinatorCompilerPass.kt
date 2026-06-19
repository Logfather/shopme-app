package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.pollinator.PollinatorResolver

class PollinatorCompilerPass(

    private val resolver: PollinatorResolver

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        val result =

            resolver.resolve(

                context.nutritionReference

            )

        context.pollinator =
            result

    }

}