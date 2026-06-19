package de.shopme.testing.system.tools.knowledge.test.enricher

import de.shopme.tools.knowledge.FoodKnowledgeAssembler
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompiler
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPipeline

class TestFoodKnowledgeCompilerBuilder {

    private val passes =

        mutableListOf<FoodKnowledgeCompilerPass>()

    fun add(

        pass: FoodKnowledgeCompilerPass

    ): TestFoodKnowledgeCompilerBuilder {

        passes += pass

        return this

    }

    fun build(): FoodKnowledgeCompiler {

        return FoodKnowledgeCompiler(

            pipeline =

                FoodKnowledgeCompilerPipeline(

                    passes

                ),

            assembler =

                FoodKnowledgeAssembler()

        )

    }

}