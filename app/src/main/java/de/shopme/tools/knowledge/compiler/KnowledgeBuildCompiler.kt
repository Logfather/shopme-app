package de.shopme.tools.knowledge.compiler

import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.compiler.writer.FoodKnowledgeWriter

class KnowledgeBuildCompiler(

    private val compiler: FoodKnowledgeCompiler,

    private val writers: List<FoodKnowledgeWriter>

) {

    fun build(

        catalog: List<CatalogItem>

    ) {

        writers.forEach {

            it.begin()

        }

        catalog.forEach { item ->

            val context =

                compiler.compileContext(

                    item

                )

            writers.forEach {

                it.write(

                    context

                )

            }

        }

        writers.forEach {

            it.finish()

        }

    }

}