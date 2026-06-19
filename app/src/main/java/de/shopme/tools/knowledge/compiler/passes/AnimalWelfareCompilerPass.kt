package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.animalwelfare.AnimalWelfareResolver
import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass

class AnimalWelfareCompilerPass(

    private val resolver: AnimalWelfareResolver

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        context.animalWelfare =

            resolver.resolve(

                context.nutritionReference

            )

    }

}