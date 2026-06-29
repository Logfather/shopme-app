package de.shopme.tools.knowledge.off

data class InvalidOFFKnowledgeProposal(

    val proposal: OFFKnowledgeProposal,

    val reasons: List<String>
)