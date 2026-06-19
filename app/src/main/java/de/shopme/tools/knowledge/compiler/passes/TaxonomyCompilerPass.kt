package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.taxonomy.FoodTaxonomyResolver

class TaxonomyCompilerPass(

    private val resolver: FoodTaxonomyResolver

) : FoodKnowledgeCompilerPass {

    override fun process(

        context: CompilerContext

    ) {

        val result =

            resolver.resolve(

                context.nutritionReference

            )

        context.foodTaxonomy =

            result

        if (result != null) {

            context.taxonomyPath +=

                result.parent

        }

    }

}