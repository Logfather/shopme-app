package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.semantics.FoodSemanticsResolver

class FoodSemanticsCompilerPass(

    private val resolver: FoodSemanticsResolver

) : FoodKnowledgeCompilerPass {

    override fun process(

        context: CompilerContext

    ) {

        val semantics =

            resolver.resolve(

                context.foodTaxonomy?.parent

            ) ?: return

        semantics.category?.let {

            context.foodCategory = it

        }

        context.tags.addAll(

            semantics.tags

        )

    }

}