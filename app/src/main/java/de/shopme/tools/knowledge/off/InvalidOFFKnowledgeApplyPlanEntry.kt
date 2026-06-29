package de.shopme.tools.knowledge.off

import de.shopme.tools.knowledge.openfoodfacts.OFFKnowledgeProposalApplyEntry

data class InvalidOFFKnowledgeApplyPlanEntry(

    val entry: OFFKnowledgeProposalApplyEntry,

    val reasons: List<String>
)