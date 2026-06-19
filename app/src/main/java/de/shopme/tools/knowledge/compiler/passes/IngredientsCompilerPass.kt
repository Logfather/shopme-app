package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.ingredients.IngredientsResolver

class IngredientsCompilerPass(

    private val resolver: IngredientsResolver

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        val result =

            resolver.resolve(

                context.nutritionReference

            )

        context.ingredients += result

    }

}