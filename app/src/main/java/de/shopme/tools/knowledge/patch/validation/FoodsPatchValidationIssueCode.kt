package de.shopme.tools.knowledge.patch.validation

enum class FoodsPatchValidationIssueCode {

    DUPLICATE_CANONICAL_ID,

    BLANK_CANONICAL_ID,

    CANONICAL_ID_MISMATCH,

    BLANK_PATCH_SOURCE,

    BLANK_PATCH_VERSION
}