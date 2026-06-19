package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.seasonality.SeasonalityResolver

class SeasonalityCompilerPass(

    private val resolver: SeasonalityResolver

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        val result =

            resolver.resolve(

                context.nutritionReference

            )

        context.seasonality +=

            result

    }

}