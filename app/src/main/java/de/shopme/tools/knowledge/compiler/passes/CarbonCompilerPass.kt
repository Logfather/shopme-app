package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.carbon.CarbonFootprintResolver
import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass

class CarbonCompilerPass(

    private val resolver: CarbonFootprintResolver

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        context.carbonFootprint =

            resolver.resolve(

                context.nutritionReference

            )

    }

}