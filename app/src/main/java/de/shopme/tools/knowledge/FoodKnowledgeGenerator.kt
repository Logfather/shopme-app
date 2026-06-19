package de.shopme.tools.knowledge

import de.shopme.domain.food.FoodKnowledgeEntry
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompiler
import de.shopme.tools.knowledge.reader.CatalogReader

class FoodKnowledgeGenerator(

    private val catalogReader: CatalogReader,
    private val compiler: FoodKnowledgeCompiler

) {

    fun generate(): List<FoodKnowledgeEntry> {

        return catalogReader
            .read()
            .map(
                compiler::compile
            )
            .sortedBy {
                it.normalizedName
            }

    }

}