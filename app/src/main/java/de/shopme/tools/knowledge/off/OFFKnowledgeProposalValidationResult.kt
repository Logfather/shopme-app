package de.shopme.tools.knowledge.off

data class OFFKnowledgeProposalValidationResult(

    val valid: List<OFFKnowledgeProposal>,

    val invalid: List<InvalidOFFKnowledgeProposal>
)