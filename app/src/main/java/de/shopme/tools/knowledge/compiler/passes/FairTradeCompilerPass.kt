package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.fairtrade.FairTradeResolver

class FairTradeCompilerPass(

    private val resolver: FairTradeResolver

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {


        context.fairTrade =

            resolver.resolve(

                context.nutritionReference

            )

    }

}