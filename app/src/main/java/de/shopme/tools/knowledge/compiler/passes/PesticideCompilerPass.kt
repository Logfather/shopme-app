package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.pesticide.PesticideResolver

class PesticideCompilerPass(

    private val resolver: PesticideResolver

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        context.pesticide =

            resolver.resolve(

                context.nutritionReference

            )

    }

}