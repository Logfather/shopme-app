package de.shopme.testing.system.tools.knowledge.runtime

import de.shopme.tools.knowledge.allergen.DefaultAllergenResolver
import de.shopme.tools.knowledge.allergen.StringAllergenLoader
import de.shopme.tools.knowledge.ingredients.DefaultIngredientsResolver
import de.shopme.tools.knowledge.ingredients.StringIngredientsLoader
import de.shopme.tools.knowledge.nutriscore.DefaultNutriScoreResolver
import de.shopme.tools.knowledge.nutriscore.StringNutriScoreLoader
import de.shopme.tools.knowledge.nutrition.DefaultNutritionFactsResolver
import de.shopme.tools.knowledge.nutrition.StringNutritionFactsLoader
import de.shopme.tools.knowledge.taxonomy.DefaultFoodTaxonomyResolver
import de.shopme.tools.knowledge.taxonomy.StringFoodTaxonomyLoader
import java.io.File
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RuntimeKnowledgeResolverIntegrationTest {

    @Test
    fun resolvesKnowledgeFromRuntimeAssets() {
        val runtimeDir =
            File("src/main/assets/knowledge/runtime")

        assertTrue(
            runtimeDir.exists(),
            "Runtime assets dir missing: ${runtimeDir.path}"
        )

        assertResolvesNutrition(runtimeDir)
        assertResolvesIngredients(runtimeDir)
        assertResolvesAllergens(runtimeDir)
        assertResolvesTaxonomy(runtimeDir)
        assertResolvesNutriScore(runtimeDir)
    }

    private fun assertResolvesNutrition(
        runtimeDir: File
    ) {
        val resolver =
            DefaultNutritionFactsResolver(
                StringNutritionFactsLoader(
                    read(
                        runtimeDir,
                        "nutrition.json"
                    )
                ).load()
            )

        assertNotNull(
            resolver.resolve("milk")
        )
    }

    private fun assertResolvesIngredients(
        runtimeDir: File
    ) {
        val resolver =
            DefaultIngredientsResolver(
                StringIngredientsLoader(
                    read(
                        runtimeDir,
                        "ingredients.json"
                    )
                ).load()
            )

        assertTrue(
            resolver.resolve("milk")
                .isNotEmpty()
        )
    }

    private fun assertResolvesAllergens(
        runtimeDir: File
    ) {
        val resolver =
            DefaultAllergenResolver(
                StringAllergenLoader(
                    read(
                        runtimeDir,
                        "allergens.json"
                    )
                ).load()
            )

        assertTrue(
            resolver.resolve("milk")
                .isNotEmpty()
        )
    }

    private fun assertResolvesTaxonomy(
        runtimeDir: File
    ) {
        val resolver =
            DefaultFoodTaxonomyResolver(
                StringFoodTaxonomyLoader(
                    read(
                        runtimeDir,
                        "food_taxonomy.json"
                    )
                ).load()
            )

        assertNotNull(
            resolver.resolve("milk")
        )
    }

    private fun assertResolvesNutriScore(
        runtimeDir: File
    ) {
        val resolver =
            DefaultNutriScoreResolver(
                StringNutriScoreLoader(
                    read(
                        runtimeDir,
                        "nutri_score.json"
                    )
                ).load()
            )

        assertNotNull(
            resolver.resolve("milk")
        )
    }

    private fun read(
        dir: File,
        name: String
    ): String {
        val file =
            File(
                dir,
                name
            )

        assertTrue(
            file.exists(),
            "Runtime asset missing: ${file.path}"
        )

        return file.readText()
    }
}