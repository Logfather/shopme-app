package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.foods.FoodLookup
import de.shopme.tools.knowledge.nutrition.NutritionFactsResolver

class NutritionCompilerPass(

    private val resolver: NutritionFactsResolver,

    private val foodLookup: FoodLookup

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        context.nutritionFacts =

            foodLookup.nutritionFacts(

                context.normalizedName

            )
                ?: resolver.resolve(

                    context.nutritionReference

                )
                        ?: resolver.resolve(

                    context.normalizedName

                )
    }

}