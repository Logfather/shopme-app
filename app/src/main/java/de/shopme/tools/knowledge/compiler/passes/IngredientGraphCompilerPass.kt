package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.ingredientgraph.IngredientGraphResolver

class IngredientGraphCompilerPass(

    private val resolver:

    IngredientGraphResolver

) : FoodKnowledgeCompilerPass {

    override fun process(

        context: CompilerContext

    ) {

        context.ingredientGraph =

            resolver.resolve(

                context.nutritionReference

            )

    }

}