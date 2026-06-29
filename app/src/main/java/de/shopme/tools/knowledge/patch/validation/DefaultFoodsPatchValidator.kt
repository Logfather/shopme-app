package de.shopme.tools.knowledge.patch.validation

import de.shopme.tools.knowledge.patch.FoodsKnowledgePatch

class DefaultFoodsPatchValidator : FoodsPatchValidator {

    override fun validate(
        patch: FoodsKnowledgePatch
    ): FoodsPatchValidationResult {

        val issues = mutableListOf<FoodsPatchValidationIssue>()

        if (patch.metadata.source.isBlank()) {
            issues += FoodsPatchValidationIssue(
                code = FoodsPatchValidationIssueCode.BLANK_PATCH_SOURCE,
                canonicalId = "<patch>",
                severity = FoodsPatchValidationSeverity.ERROR,
                message = "Patch metadata source must not be blank."
            )
        }

        if (patch.metadata.version.isBlank()) {
            issues += FoodsPatchValidationIssue(
                code = FoodsPatchValidationIssueCode.BLANK_PATCH_VERSION,
                canonicalId = "<patch>",
                severity = FoodsPatchValidationSeverity.ERROR,
                message = "Patch metadata version must not be blank."
            )
        }

        patch.entries
            .groupBy { it.canonicalId }
            .filterValues { it.size > 1 }
            .keys
            .sorted()
            .forEach { canonicalId ->
                issues += FoodsPatchValidationIssue(
                    code = FoodsPatchValidationIssueCode.DUPLICATE_CANONICAL_ID,
                    canonicalId = canonicalId,
                    severity = FoodsPatchValidationSeverity.ERROR,
                    message = "Duplicate canonicalId in patch."
                )
            }

        patch.entries
            .filter { it.canonicalId != it.candidate.canonicalId }
            .sortedBy { it.canonicalId }
            .forEach { entry ->
                issues += FoodsPatchValidationIssue(
                    code = FoodsPatchValidationIssueCode.CANONICAL_ID_MISMATCH,
                    canonicalId = entry.canonicalId,
                    severity = FoodsPatchValidationSeverity.ERROR,
                    message = "Patch entry canonicalId does not match candidate canonicalId."
                )
            }

        patch.entries
            .filter { it.canonicalId.isBlank() }
            .forEach { entry ->
                issues += FoodsPatchValidationIssue(
                    code = FoodsPatchValidationIssueCode.BLANK_CANONICAL_ID,
                    canonicalId = entry.canonicalId,
                    severity = FoodsPatchValidationSeverity.ERROR,
                    message = "Patch entry canonicalId must not be blank."
                )
            }

        return FoodsPatchValidationResult(
            issues = issues
        )
    }
}