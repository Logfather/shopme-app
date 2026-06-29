package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.foods.FoodLookup
import de.shopme.tools.knowledge.seasonality.SeasonalityResolver

class SeasonalityCompilerPass(

    private val resolver: SeasonalityResolver,

    private val foodLookup: FoodLookup

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        val result =

            foodLookup.seasonality(

                context.normalizedName

            )
                ?: resolver.resolve(

                    context.nutritionReference
                        ?: context.normalizedName

                )

        context.seasonality.clear()

        context.seasonality.addAll(result)
    }
}