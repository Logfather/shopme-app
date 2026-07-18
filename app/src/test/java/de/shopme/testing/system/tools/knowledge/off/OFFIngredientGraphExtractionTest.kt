package de.shopme.testing.system.tools.knowledge.off

import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import de.shopme.tools.knowledge.off.extractor.OFFCandidateExtractor
import java.io.File
import java.util.zip.GZIPOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OFFIngredientGraphExtractionTest {

    @Test
    fun extractsIngredientGraphFromIngredientsTags() {
        val file =
            File.createTempFile(
                "off-ingredient-graph-fixture",
                ".jsonl.gz"
            )

        GZIPOutputStream(
            file.outputStream()
        ).bufferedWriter()
            .use { writer ->
                writer.write(
                    """
                    {
                      "code": "123",
                      "product_name_en": "Pizza",
                      "brands": "Test Brand",
                      "categories": "Pizza",
                      "ingredients_tags": [
                        "en:tomato",
                        "en:cheese",
                        "en:wheat-flour"
                      ]
                    }
                    """.trimIndent()
                        .replace("\n", "")
                )
                writer.newLine()
            }

        val candidate =
            OFFCandidateExtractor()
                .extract(
                    file = file,
                    maxCandidates = 1
                )
                .single()

        val dimension =
            candidate.dimensions
                .firstOrNull { dimension ->
                    dimension.dimension ==
                            KnowledgeDimensionCandidateType.INGREDIENT_GRAPH
                }

        assertNotNull(
            dimension
        )

        assertEquals(
            mapOf(
                "ingredients" to
                        listOf(
                            "cheese",
                            "tomato",
                            "wheat flour"
                        )
            ),
            dimension.payload
        )
    }

    @Test
    fun extractsIngredientGraphFromIngredientsTextFallback() {
        val file =
            File.createTempFile(
                "off-ingredient-graph-text-fixture",
                ".jsonl.gz"
            )

        GZIPOutputStream(
            file.outputStream()
        ).bufferedWriter()
            .use { writer ->
                writer.write(
                    """
                {
                  "code": "124",
                  "product_name_en": "Tomato Sauce",
                  "brands": "Test Brand",
                  "categories": "Sauce",
                  "ingredients_text_en": "Tomatoes, olive oil, basil, salt"
                }
                """.trimIndent()
                        .replace("\n", "")
                )
                writer.newLine()
            }

        val candidate =
            OFFCandidateExtractor()
                .extract(
                    file = file,
                    maxCandidates = 1
                )
                .single()

        val dimension =
            candidate.dimensions
                .firstOrNull { dimension ->
                    dimension.dimension ==
                            KnowledgeDimensionCandidateType.INGREDIENT_GRAPH
                }

        assertNotNull(
            dimension
        )

        assertEquals(
            mapOf(
                "ingredients" to
                        listOf(
                            "basil",
                            "olive oil",
                            "salt",
                            "tomatoes"
                        )
            ),
            dimension.payload
        )
    }
}