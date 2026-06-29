package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.foods.FoodLookup
import de.shopme.tools.knowledge.taxonomy.FoodTaxonomyResolver

class TaxonomyCompilerPass(

    private val resolver: FoodTaxonomyResolver,

    private val foodLookup: FoodLookup

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        val taxonomy =

            resolver.resolve(

                context.nutritionReference
                    ?: context.normalizedName

            )

        val path =

            foodLookup.taxonomy(

                context.normalizedName

            )
                ?: taxonomy?.let { entry ->

                    listOf(

                        entry.parent,

                        context.normalizedName

                    )
                }
                ?: emptyList()

        context.taxonomyPath.clear()

        context.taxonomyPath.addAll(path)
    }
}