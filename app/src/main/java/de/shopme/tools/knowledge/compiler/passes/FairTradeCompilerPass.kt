package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.fairtrade.FairTradeResolver
import de.shopme.tools.knowledge.foods.FoodLookup

class FairTradeCompilerPass(

    private val resolver: FairTradeResolver,

    private val foodLookup: FoodLookup

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        context.fairTrade =

            foodLookup.fairTrade(

                context.normalizedName

            )
                ?: resolver.resolve(

                    context.nutritionReference
                        ?: context.normalizedName

                )
    }
}