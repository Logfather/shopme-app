package de.shopme.tools.knowledge.patch

data class FoodsPatchDiffEntry(

    val canonicalId: String,

    val operation: FoodsPatchDiffOperation
)