package de.shopme.testing.system.tools.knowledge.agribalyse

import de.shopme.tools.knowledge.agribalyse.extractor.AgribalyseCandidateExtractor
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AgribalyseCandidateExtractorTest {

    @Test
    fun extractCandidatesFromSlimTsv() {

        val file =
            File(
                "../data/generated/agribalyse/" +
                        "agribalyse-foods.slim.tsv"
            )

        val candidates =
            AgribalyseCandidateExtractor()
                .extract(
                    file = file,
                    maxCandidates = 50_000
                )

        val withEnvironmentalImpact =
            candidates.count { candidate ->
                candidate.dimensions.any { dimension ->
                    dimension.dimension ==
                            KnowledgeDimensionCandidateType.ENVIRONMENTAL_IMPACT
                }
            }

        println("Agribalyse extracted=${candidates.size}")
        println(
            "Agribalyse with environmental impact=$withEnvironmentalImpact"
        )
        println("Sample=${candidates.take(10)}")
        println("Sample metadata=${candidates.firstOrNull()?.metadata}")

        assertTrue(candidates.isNotEmpty())
        assertTrue(withEnvironmentalImpact > 0)

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

        assertEquals(
            candidates.size,
            withEnvironmentalImpact
        )
    }
}