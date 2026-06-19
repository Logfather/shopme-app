package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.waterstress.WaterStressResolver

class WaterStressCompilerPass(

    private val resolver:

    WaterStressResolver

) : FoodKnowledgeCompilerPass {

    override fun process(

        context: CompilerContext

    ) {

        context.waterStress =

            resolver.resolve(

                context.nutritionReference

            )

    }

}