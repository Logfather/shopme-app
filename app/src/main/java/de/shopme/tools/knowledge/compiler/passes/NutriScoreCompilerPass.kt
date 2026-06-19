package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.nutriscore.NutriScoreResolver

class NutriScoreCompilerPass(

    private val resolver: NutriScoreResolver

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        context.nutriScore =

            resolver.resolve(

                context.nutritionReference

            )

    }

}