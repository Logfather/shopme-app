package de.shopme.tools.knowledge.off

class OFFKnowledgeProposalValidator {

    fun validate(
        proposals: List<OFFKnowledgeProposal>
    ): OFFKnowledgeProposalValidationResult {

        val valid =
            mutableListOf<OFFKnowledgeProposal>()

        val invalid =
            mutableListOf<InvalidOFFKnowledgeProposal>()

        proposals.forEach { proposal ->

            val reasons =
                validateProposal(proposal)

            if (reasons.isEmpty()) {

                valid += proposal

            } else {

                invalid += InvalidOFFKnowledgeProposal(
                    proposal = proposal,
                    reasons = reasons
                )
            }
        }

        return OFFKnowledgeProposalValidationResult(
            valid = valid,
            invalid = invalid
        )
    }

    private fun validateProposal(
        proposal: OFFKnowledgeProposal
    ): List<String> {

        val reasons =
            mutableListOf<String>()

        if (proposal.catalogNormalizedName.isBlank()) {
            reasons += "catalogNormalizedName is blank"
        }

        if (proposal.offCode.isNullOrBlank()) {
            reasons += "offCode is blank"
        }

        if (proposal.offProductName.isBlank()) {
            reasons += "offProductName is blank"
        }

        if (proposal.source.isBlank()) {
            reasons += "source is blank"
        }

        if (proposal.dimensions.isEmpty()) {
            reasons += "dimensions is empty"
        }

        if (proposal.proposedReferences.isEmpty()) {
            reasons += "proposedReferences is empty"
        }

        val missingReferences =
            proposal.dimensions
                .filterNot {
                    proposal.proposedReferences.containsKey(it)
                }

        if (missingReferences.isNotEmpty()) {
            reasons += "missing proposedReferences for dimensions: ${
                missingReferences.joinToString(",")
            }"
        }

        val invalidReferences =
            proposal.proposedReferences
                .filterValues {
                    it.isBlank() || !it.startsWith("off:")
                }

        if (invalidReferences.isNotEmpty()) {
            reasons += "invalid proposedReferences: ${
                invalidReferences.keys.joinToString(",")
            }"
        }

        return reasons
    }
}