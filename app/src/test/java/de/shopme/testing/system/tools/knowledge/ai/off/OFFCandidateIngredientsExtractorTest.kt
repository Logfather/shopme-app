package de.shopme.testing.system.tools.knowledge.ai.off

import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import de.shopme.tools.knowledge.off.extractor.OFFCandidateExtractor
import org.junit.Assert.assertTrue
import java.io.File
import kotlin.test.Test
import kotlin.test.assertNotNull

class OFFCandidateIngredientsExtractorTest {

    @Test
    fun extractsIngredientsDimensionFromOFFProduct() {
        val candidates =
            OFFCandidateExtractor()
                .extract(
                    file = File(
                        "../data/generated/openfoodfacts/openfoodfacts-products.slim.jsonl.gz"
                    ),
                    maxCandidates = 1_000
                )

        val withIngredients =
            candidates.firstOrNull { candidate ->
                candidate.dimensions.any { dimension ->
                    dimension.dimension == KnowledgeDimensionCandidateType.INGREDIENTS
                }
            }

        assertNotNull(withIngredients)

        val ingredientsDimension =
            withIngredients.dimensions.first { dimension ->
                dimension.dimension == KnowledgeDimensionCandidateType.INGREDIENTS
            }

        val payload =
            ingredientsDimension.payload as Map<*, *>

        assertTrue(
            payload.containsKey("ingredientsText") ||
                    payload.containsKey("ingredients")
        )
    }

}