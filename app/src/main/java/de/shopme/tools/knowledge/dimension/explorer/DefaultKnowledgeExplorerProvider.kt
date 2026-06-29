package de.shopme.tools.knowledge.dimension.explorer

import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompiler
import de.shopme.tools.knowledge.dimension.DefaultKnowledgeDimensionRegistry

class DefaultKnowledgeExplorerProvider(

    private val compiler: FoodKnowledgeCompiler,

    private val builder: KnowledgeExplorerBuilder =
        KnowledgeExplorerBuilder()

) : KnowledgeExplorerProvider {

    private val registry =
        DefaultKnowledgeDimensionRegistry.create()

    override fun create(
        catalogItem: CatalogItem
    ): KnowledgeExplorerModel {

        val knowledge =
            compiler.compile(
                catalogItem
            )

        return builder.build(
            registry = registry,
            knowledge = knowledge
        ).copy(
            productName = catalogItem.itemname
        )
    }
}