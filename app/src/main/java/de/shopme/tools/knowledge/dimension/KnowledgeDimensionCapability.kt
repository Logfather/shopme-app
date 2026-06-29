package de.shopme.tools.knowledge.dimension

import de.shopme.domain.food.FoodKnowledgeEntry
import de.shopme.tools.knowledge.dimension.KnowledgeSection
import de.shopme.tools.report.CoverageDimension

interface KnowledgeDimensionCapability {

    val id: KnowledgeDimensionId

    val coverageDimension: CoverageDimension

    val section: KnowledgeSection

    val order: Int

    fun info(): KnowledgeDimensionInfo

    fun result(
        knowledge: FoodKnowledgeEntry
    ): KnowledgeDimensionResult

}