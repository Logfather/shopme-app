package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.allergen.AllergenResolver
import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.foods.FoodLookup

class AllergenCompilerPass(

    private val resolver: AllergenResolver,

    private val foodLookup: FoodLookup

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        val result =
            foodLookup.allergens(
                context.normalizedName
            )
                ?: resolver.resolve(
                    context.nutritionReference
                        ?: context.normalizedName
                )

        context.allergens += result
    }
}