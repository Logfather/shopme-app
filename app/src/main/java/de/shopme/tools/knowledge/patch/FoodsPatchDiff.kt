package de.shopme.tools.knowledge.patch

data class FoodsPatchDiff(

    val entries: List<FoodsPatchDiffEntry>,

    val stats: FoodsPatchDiffStats

)