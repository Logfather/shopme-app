package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.carbon.CarbonFootprintResolver
import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.foods.FoodLookup

class CarbonCompilerPass(

    private val resolver: CarbonFootprintResolver,

    private val foodLookup: FoodLookup

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        val carbonReference =

            foodLookup.carbonReference(

                context.normalizedName

            )
                ?: context.nutritionReference
                ?: context.normalizedName

        context.carbonReference =
            carbonReference

        context.carbonFootprint =

            foodLookup.carbonFootprint(

                context.normalizedName

            )
                ?: resolver.resolve(

                    carbonReference

                )
    }
}