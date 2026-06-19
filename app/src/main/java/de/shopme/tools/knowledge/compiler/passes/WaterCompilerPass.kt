package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.waterfootprint.WaterResolver

class WaterCompilerPass(

    private val resolver: WaterResolver

) : FoodKnowledgeCompilerPass {

    override fun process(

        context: CompilerContext

    ) {

        context.waterFootprint =

            resolver.resolve(

                context.nutritionReference

            )

    }

}