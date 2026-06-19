package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.nutrition.NutritionAliasResolver

class NutritionAliasCompilerPass(

    private val resolver: NutritionAliasResolver

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        if (context.nutritionReference != null) {
            return
        }

        context.nutritionReference =

            resolver.resolve(

                context.normalizedName

            )

    }

}