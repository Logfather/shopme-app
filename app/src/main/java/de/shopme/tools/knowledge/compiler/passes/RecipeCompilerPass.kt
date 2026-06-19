package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.recipe.RecipeResolver

class RecipeCompilerPass(

    private val resolver: RecipeResolver

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        context.recipes +=

            resolver.resolve(

                context.nutritionReference

            )

    }

}