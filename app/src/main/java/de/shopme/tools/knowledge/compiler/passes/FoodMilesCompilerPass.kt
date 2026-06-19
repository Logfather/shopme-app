package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.foodmiles.FoodMilesResolver

class FoodMilesCompilerPass(

    private val resolver:

    FoodMilesResolver

) : FoodKnowledgeCompilerPass {

    override fun process(

        context: CompilerContext

    ) {

        val result =

            resolver.resolve(

                context.nutritionReference

            )

        context.foodMiles =

            result

    }

}