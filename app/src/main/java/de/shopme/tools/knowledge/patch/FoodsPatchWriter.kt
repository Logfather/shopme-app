package de.shopme.tools.knowledge.patch

interface FoodsPatchWriter {

    fun write(
        result: FoodsPatchApplyResult,
        outputFile: String
    ): FoodsPatchWriteResult
}