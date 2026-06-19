package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.recipegraph.RecipeGraphResolver

class RecipeGraphCompilerPass(

    private val resolver: RecipeGraphResolver

) : FoodKnowledgeCompilerPass {

    override fun process(

        context: CompilerContext

    ) {

        context.recipeGraph =

            resolver.resolve(

                context.nutritionReference

            )

    }

}