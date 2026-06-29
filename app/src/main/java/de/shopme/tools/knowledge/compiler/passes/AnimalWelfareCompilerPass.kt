package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.animalwelfare.AnimalWelfareResolver
import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.foods.FoodLookup

class AnimalWelfareCompilerPass(

    private val resolver: AnimalWelfareResolver,

    private val foodLookup: FoodLookup

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        context.animalWelfare =

            foodLookup.animalWelfare(

                context.normalizedName

            )
                ?: resolver.resolve(

                    context.nutritionReference
                        ?: context.normalizedName

                )
    }
}