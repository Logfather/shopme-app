package de.shopme.tools.knowledge.off

import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId
import de.shopme.tools.knowledge.openfoodfacts.OFFKnowledgeProposalApplyEntry

class OFFKnowledgeApplyPlanValidator {

    fun validate(
        plan: OFFKnowledgeProposalApplyPlan
    ): OFFKnowledgeApplyPlanValidationResult {

        val invalid =
            mutableListOf<InvalidOFFKnowledgeApplyPlanEntry>()

        val duplicates =
            plan.entries
                .groupBy {
                    it.catalogNormalizedName to it.dimension
                }
                .filterValues {
                    it.size > 1
                }
                .keys

        plan.entries.forEach { entry ->

            val reasons =
                validateEntry(
                    entry = entry,
                    duplicates = duplicates
                )

            if (reasons.isNotEmpty()) {

                invalid += InvalidOFFKnowledgeApplyPlanEntry(
                    entry = entry,
                    reasons = reasons
                )
            }
        }

        return OFFKnowledgeApplyPlanValidationResult(
            validEntries =
                plan.entries.filterNot { entry ->
                    invalid.any {
                        it.entry == entry
                    }
                },

            invalidEntries =
                invalid
        )
    }

    private fun validateEntry(
        entry: OFFKnowledgeProposalApplyEntry,
        duplicates: Set<Pair<String, KnowledgeDimensionId>>
    ): List<String> {

        val reasons =
            mutableListOf<String>()

        if (entry.catalogNormalizedName.isBlank()) {
            reasons += "catalogNormalizedName is blank"
        }

        if (entry.reference.isBlank()) {
            reasons += "reference is blank"
        }

        if (!entry.reference.startsWith("off:")) {
            reasons += "reference must start with off:"
        }

        if (entry.source.isBlank()) {
            reasons += "source is blank"
        }

        if (entry.source != "off") {
            reasons += "source must be off"
        }

        if (entry.offCode.isNullOrBlank()) {
            reasons += "offCode is blank"
        }

        if (entry.offProductName.isBlank()) {
            reasons += "offProductName is blank"
        }

        if ((entry.catalogNormalizedName to entry.dimension) in duplicates) {
            reasons += "duplicate catalogNormalizedName + dimension"
        }

        return reasons
    }
}