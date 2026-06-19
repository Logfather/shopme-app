package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.biodiversity.BiodiversityResolver
import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass

class BiodiversityCompilerPass(

    private val resolver:

    BiodiversityResolver

) : FoodKnowledgeCompilerPass {

    override fun process(

        context: CompilerContext

    ) {

        val result =

            resolver.resolve(

                context.nutritionReference

            )

        context.biodiversity =

            result

    }

}