package de.shopme.tools.knowledge.compiler

import de.shopme.tools.knowledge.FoodKnowledgeAssembler
import de.shopme.tools.knowledge.compiler.passes.NutritionAliasCompilerPass
import de.shopme.tools.knowledge.nutrition.DefaultNutritionAliasResolver

object DefaultFoodKnowledgeBuildCompiler {

    fun create(

        pipeline: FoodKnowledgeCompilerPipeline

    ): FoodKnowledgeCompiler {

        return FoodKnowledgeCompiler(

            pipeline = pipeline,

            assembler = FoodKnowledgeAssembler()

        )

    }

    fun create(): FoodKnowledgeCompiler {

        return create(

            FoodKnowledgeCompilerPipeline(

                listOf(

                    NutritionAliasCompilerPass(

                        DefaultNutritionAliasResolver(

                            emptyMap()

                        )

                    )

                )

            )

        )

    }

}