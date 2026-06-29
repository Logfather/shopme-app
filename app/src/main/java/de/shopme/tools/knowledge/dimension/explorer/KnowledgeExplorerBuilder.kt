package de.shopme.tools.knowledge.dimension.explorer

import de.shopme.domain.food.FoodKnowledgeEntry
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionRegistry

class KnowledgeExplorerBuilder {

    fun build(
        registry: KnowledgeDimensionRegistry,
        knowledge: FoodKnowledgeEntry
    ): KnowledgeExplorerModel {

        val capabilities =
            registry.capabilities

        val nutriScoreCapability =
            capabilities.firstOrNull {
                it.id == KnowledgeDimensionId.NUTRI_SCORE
            }

        val nutriScore =
            nutriScoreCapability?.result(knowledge)

        val sections =
            capabilities
                .filterNot {
                    it.id == KnowledgeDimensionId.NUTRI_SCORE
                }
                .groupBy {
                    it.section
                }
                .map { (section, sectionCapabilities) ->

                    KnowledgeExplorerSection(
                        section = section,
                        dimensions =
                            sectionCapabilities
                                .sortedBy {
                                    it.order
                                }
                                .map { capability ->

                                    KnowledgeExplorerDimension(
                                        id = capability.id,
                                        info = capability.info(),
                                        result = capability.result(knowledge)
                                    )
                                }
                    )
                }
                .sortedBy {
                    it.section.order
                }

        return KnowledgeExplorerModel(
            productName = knowledge.normalizedName,
            nutriScore = nutriScore,
            sections = sections
        )
    }
}