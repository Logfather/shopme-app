package de.shopme.tools.knowledge.gap

import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId

data class CatalogKnowledgeGap(

    val normalizedName: String,

    val missingDimensions: Set<KnowledgeDimensionId>
)