package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.nutrition.NutritionFactsResolver

class NutritionCompilerPass(

    private val resolver: NutritionFactsResolver

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        context.nutritionFacts =

            resolver.resolve(

                context.nutritionReference

            )

    }

}