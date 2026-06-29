package de.shopme.tools.knowledge.off

import de.shopme.tools.knowledge.openfoodfacts.OFFKnowledgeProposalApplyEntry

data class OFFKnowledgeApplyPlanValidationResult(

    val validEntries: List<OFFKnowledgeProposalApplyEntry>,

    val invalidEntries: List<InvalidOFFKnowledgeApplyPlanEntry>
)