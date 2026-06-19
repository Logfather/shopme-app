package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.allergen.AllergenResolver
import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass

class AllergenCompilerPass(

    private val resolver: AllergenResolver

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        context.allergens +=

            resolver.resolve(

                context.nutritionReference

            )

    }

}