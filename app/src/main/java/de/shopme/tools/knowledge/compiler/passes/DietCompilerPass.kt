package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.diet.DietResolver

class DietCompilerPass(

    private val resolver: DietResolver

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        val result =

            resolver.resolve(

                context.nutritionReference

            )

        context.dietClassifications +=

            result

    }

}