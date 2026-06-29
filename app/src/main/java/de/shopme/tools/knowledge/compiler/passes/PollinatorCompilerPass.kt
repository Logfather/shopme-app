package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.foods.FoodLookup
import de.shopme.tools.knowledge.pollinator.PollinatorResolver

class PollinatorCompilerPass(

    private val resolver: PollinatorResolver,

    private val foodLookup: FoodLookup

): FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        context.pollinator =

            foodLookup.pollinator(

                context.normalizedName

            )
                ?: resolver.resolve(

                    context.nutritionReference
                        ?: context.normalizedName

                )
    }

}