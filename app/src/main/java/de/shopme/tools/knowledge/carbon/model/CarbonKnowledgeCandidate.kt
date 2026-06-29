package de.shopme.tools.knowledge.carbon.model

import de.shopme.tools.knowledge.source.KnowledgeCandidate
import de.shopme.tools.knowledge.source.KnowledgeSourceId

data class CarbonKnowledgeCandidate(

    override val reference: String,

    val kgCo2ePerKg: Double,

    override val source: KnowledgeSourceId

) : KnowledgeCandidate