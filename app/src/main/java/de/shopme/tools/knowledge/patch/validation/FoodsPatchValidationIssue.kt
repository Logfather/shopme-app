package de.shopme.tools.knowledge.patch.validation

data class FoodsPatchValidationIssue(

    val code: FoodsPatchValidationIssueCode,

    val canonicalId: String,

    val severity: FoodsPatchValidationSeverity,

    val message: String
)