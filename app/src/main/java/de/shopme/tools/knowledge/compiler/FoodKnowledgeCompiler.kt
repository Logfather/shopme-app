package de.shopme.tools.knowledge.compiler

import de.shopme.domain.catalog.CatalogItem
import de.shopme.domain.food.FoodKnowledgeEntry
import de.shopme.tools.knowledge.FoodKnowledgeAssembler

class FoodKnowledgeCompiler(

    private val pipeline: FoodKnowledgeCompilerPipeline,

    private val assembler: FoodKnowledgeAssembler

) {

    fun compile(

        item: CatalogItem

    ): FoodKnowledgeEntry {

        return assembler.build(

            compileContext(

                item

            )

        )

    }

    fun compileContext(

        item: CatalogItem

    ): CompilerContext {

        val context =

            CompilerContext(

                catalogItem = item

            )

        pipeline.process(

            context

        )

        return context

    }

}