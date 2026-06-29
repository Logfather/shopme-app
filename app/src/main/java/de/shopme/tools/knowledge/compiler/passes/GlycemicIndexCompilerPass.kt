package de.shopme.tools.knowledge.compiler.passes

import de.shopme.domain.food.GlycemicIndexLevel
import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.foods.FoodLookup
import de.shopme.tools.knowledge.glycemic.GlycemicIndexResolver

class GlycemicIndexCompilerPass(

    private val resolver: GlycemicIndexResolver,

    private val foodLookup: FoodLookup

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        context.glycemicIndex =

            foodLookup.glycemicIndex(
                context.normalizedName
            )
                ?: resolver.resolve(
                    context.nutritionReference
                        ?: context.normalizedName
                )
                        ?: GlycemicIndexLevel.UNKNOWN
    }
}