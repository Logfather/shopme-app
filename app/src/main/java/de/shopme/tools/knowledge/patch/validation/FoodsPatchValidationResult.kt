package de.shopme.tools.knowledge.patch.validation

data class FoodsPatchValidationResult(

    val issues: List<FoodsPatchValidationIssue>
) {

    val isValid: Boolean =
        issues.none { issue ->
            issue.severity == FoodsPatchValidationSeverity.ERROR
        }
}