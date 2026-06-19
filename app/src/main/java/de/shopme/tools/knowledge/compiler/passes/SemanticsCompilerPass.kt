package de.shopme.tools.knowledge.compiler.passes

import de.shopme.domain.food.FoodCategory
import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.semantics.FoodSemanticsResolver
import de.shopme.tools.knowledge.taxonomy.FoodTaxonomyResolver

class SemanticsCompilerPass(

    private val taxonomyResolver: FoodTaxonomyResolver,

    private val semanticsResolver: FoodSemanticsResolver

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        var current =
            context.nutritionReference

        while (current != null) {

            context.taxonomyPath += current

            val semantics =

                semanticsResolver.resolve(
                    current
                )

            if (semantics != null) {

                if (
                    context.foodCategory ==
                    FoodCategory.UNKNOWN &&
                    semantics.category != null
                ) {

                    context.foodCategory =
                        semantics.category

                }

                context.tags +=
                    semantics.tags

            }

            current =
                taxonomyResolver
                    .resolve(current)
                    ?.parent

        }

    }

}