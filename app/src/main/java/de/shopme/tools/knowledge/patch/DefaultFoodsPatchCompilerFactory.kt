package de.shopme.tools.knowledge.patch

import de.shopme.tools.knowledge.patch.validation.DefaultFoodsPatchValidator

object DefaultFoodsPatchCompilerFactory {

    fun create(): FoodsPatchCompiler {

        return FoodsPatchCompiler(
            validator = DefaultFoodsPatchValidator(),
            mergeEngine = FoodsPatchMergeEngine()
        )
    }
}

object DefaultFoodsPatchApplierFactory {

    fun create(): FoodsPatchApplier {

        return FoodsPatchApplier(

            compiler = DefaultFoodsPatchCompilerFactory.create(),

            diffCalculator = FoodsPatchDiffCalculator()

        )
    }
}