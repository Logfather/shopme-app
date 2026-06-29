package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.foods.FoodLookup
import de.shopme.tools.knowledge.waterstress.WaterStressResolver

class WaterStressCompilerPass(

    private val resolver: WaterStressResolver,

    private val foodLookup: FoodLookup

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        context.waterStress =

            foodLookup.waterStress(

                context.normalizedName

            )
                ?: resolver.resolve(

                    context.nutritionReference
                        ?: context.normalizedName

                )
    }
}