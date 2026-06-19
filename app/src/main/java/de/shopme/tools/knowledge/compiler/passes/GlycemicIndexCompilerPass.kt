package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.glycemic.GlycemicIndexResolver

class GlycemicIndexCompilerPass(

    private val resolver: GlycemicIndexResolver

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        context.glycemicIndex =

            resolver.resolve(

                context.nutritionReference

            )

    }

}