package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.biodiversity.BiodiversityResolver
import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.foods.FoodLookup

class BiodiversityCompilerPass(

    private val resolver: BiodiversityResolver,

    private val foodLookup: FoodLookup

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        context.biodiversity =

            foodLookup.biodiversity(

                context.normalizedName

            )
                ?: resolver.resolve(

                    context.nutritionReference
                        ?: context.normalizedName

                )
    }
}