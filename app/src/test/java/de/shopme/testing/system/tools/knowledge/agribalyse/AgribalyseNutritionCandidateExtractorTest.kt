package de.shopme.testing.system.tools.knowledge.agribalyse

import de.shopme.tools.knowledge.agribalyse.extractor.AgribalyseNutritionCandidateExtractor
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class AgribalyseNutritionCandidateExtractorTest {

    @Test
    fun extractsNutritionCandidatesFromAgribalyse() {

        val file =
            File("../data/raw/agribalyse/AGRIBALYSE3.2_Tableur produits alimentaires_PublieAOUT25.xlsx")

        val candidates =
            AgribalyseNutritionCandidateExtractor()
                .extract(
                    file = file,
                    maxCandidates = 50_000
                )

        val withNutrition =
            candidates.count { candidate ->
                candidate.dimensions.any { dimension ->
                    dimension.dimension == KnowledgeDimensionCandidateType.NUTRITION
                }
            }

        println("Agribalyse extracted=${candidates.size}")
        println("Agribalyse with nutrition=$withNutrition")
        println("Sample=${candidates.take(10)}")

        assertTrue(candidates.isNotEmpty())
        assertTrue(withNutrition > 0)

        assertTrue(
            candidates.all { candidate ->
                candidate.metadata.source == "agribalyse"
            }
        )

        assertTrue(
            candidates.all { candidate ->
                candidate.canonicalId.isNotBlank()
            }
        )
    }
}