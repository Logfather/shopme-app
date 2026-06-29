package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.foods.FoodLookup
import de.shopme.tools.knowledge.pesticide.PesticideResolver

class PesticideCompilerPass(

    private val resolver: PesticideResolver,

    private val foodLookup: FoodLookup

)  : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        context.pesticide =

            foodLookup.pesticide(

                context.normalizedName

            )
                ?: resolver.resolve(

                    context.nutritionReference
                        ?: context.normalizedName

                )
    }

}