package de.shopme.tools.knowledge.patch.validation

import de.shopme.tools.knowledge.patch.FoodsKnowledgePatch

interface FoodsPatchValidator {

    fun validate(
        patch: FoodsKnowledgePatch
    ): FoodsPatchValidationResult
}