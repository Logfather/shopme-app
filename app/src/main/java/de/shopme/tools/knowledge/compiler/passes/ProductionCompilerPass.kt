package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.production.ProductionResolver

class ProductionCompilerPass(

    private val resolver: ProductionResolver

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        val result =

            resolver.resolve(

                context.nutritionReference

            )

        context.production += result

    }

}