package de.shopme.tools.knowledge.dimension.explorer

import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionInfo
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionResult
import de.shopme.tools.knowledge.dimension.KnowledgeSection

data class KnowledgeExplorerModel(

    val productName: String,

    val nutriScore: KnowledgeDimensionResult?,

    val sections: List<KnowledgeExplorerSection>

)

data class KnowledgeExplorerSection(

    val section: KnowledgeSection,

    val dimensions: List<KnowledgeExplorerDimension>

)

data class KnowledgeExplorerDimension(

    val id: KnowledgeDimensionId,

    val info: KnowledgeDimensionInfo,

    val result: KnowledgeDimensionResult

)