package de.shopme.testing.system.tools.knowledge.off

import de.shopme.tools.knowledge.ki_candidates.KnowledgeCandidateMerger
import de.shopme.tools.knowledge.ki_candidates.KnowledgeConflictResolutionType
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import de.shopme.tools.knowledge.ki_candidates.normalizer.KnowledgeCandidateNormalizer
import de.shopme.tools.knowledge.off.extractor.OFFCandidateExtractor
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class OFFCandidateExtractorTest {

    @Test
    fun extractCandidatesFromOpenFoodFacts() {

        val extracted =
            OFFCandidateExtractor()
                .extract(
                    file = File("../data/generated/openfoodfacts/openfoodfacts-products.slim.jsonl.gz"),
                    maxCandidates = 50_000
                )

        val normalized =
            KnowledgeCandidateNormalizer()
                .normalize(extracted)

        val mergeResult =
            KnowledgeCandidateMerger()
                .merge(normalized)

        val merged =
            mergeResult.candidates

        val withNutrition =
            extracted.count { candidate ->
                candidate.dimensions.any { dimension ->
                    dimension.dimension == KnowledgeDimensionCandidateType.NUTRITION
                }
            }

        val nutritionConflicts =
            mergeResult.conflicts.filter { conflict ->
                conflict.dimension == KnowledgeDimensionCandidateType.NUTRITION
            }

        val nutritionResolutions =
            nutritionConflicts.mapNotNull { conflict ->
                conflict.resolution
            }

        val lowConfidenceConflicts =
            nutritionConflicts.filter { conflict ->
                conflict.resolution?.confidence?.name == "LOW"
            }

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("LOW CONFIDENCE NUTRITION CONFLICTS")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("count=${lowConfidenceConflicts.size}")

        lowConfidenceConflicts
            .take(50)
            .forEach { conflict ->

                println()
                println("needsReview:")
                println("id=${conflict.canonicalId}")
                println("alternatives=${conflict.resolution?.alternatives}")
                println("selectedScore=${conflict.resolution?.selectedScore}")

                println("selected:")
                println(conflict.selectedPayload)
            }

        println("OFF extracted=${extracted.size}")
        println("OFF normalized=${normalized.size}")
        println("OFF merged=${merged.size}")
        println("OFF conflicts=${mergeResult.conflicts.size}")

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("OFF NUTRITION RESOLUTION REPORT")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("nutritionConflicts=${nutritionConflicts.size}")
        println("resolvedNutritionConflicts=${nutritionResolutions.size}")

        nutritionResolutions
            .groupingBy { resolution ->
                resolution.type
            }
            .eachCount()
            .forEach { (type, count) ->
                println("resolutionType.$type=$count")
            }

        nutritionResolutions
            .groupingBy { resolution ->
                resolution.confidence
            }
            .eachCount()
            .forEach { (confidence, count) ->
                println("resolutionConfidence.$confidence=$count")
            }

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("OFF MERGE CONFLICT SAMPLE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        mergeResult.conflicts
            .take(20)
            .forEach { conflict ->

                println()
                println("ID:")
                println(conflict.canonicalId)

                println("Dimension:")
                println(conflict.dimension)

                println("Resolution:")
                println("type=${conflict.resolution?.type}")
                println("alternatives=${conflict.resolution?.alternatives}")
                println("selectedScore=${conflict.resolution?.selectedScore}")
                println("confidence=${conflict.resolution?.confidence}")

                println("Selected payload:")
                println(conflict.selectedPayload)

                println("Rejected payloads:")

                conflict.rejectedPayloads
                    .forEachIndexed { index: Int, payload: Any ->
                        println("[$index]")
                        println(payload)
                    }
            }

        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        println("OFF with nutrition=$withNutrition")
        println("Sample=${merged.take(10)}")
        println("Sample metadata=${merged.first().metadata}")

        assertTrue(extracted.isNotEmpty())
        assertTrue(normalized.isNotEmpty())
        assertTrue(merged.isNotEmpty())
        assertTrue(merged.size <= extracted.size)
        assertTrue(withNutrition > 0)

        assertTrue(nutritionConflicts.isNotEmpty())
        assertTrue(nutritionResolutions.isNotEmpty())

        assertTrue(
            nutritionResolutions.all { resolution ->
                resolution.type == KnowledgeConflictResolutionType.QUALITY_SCORE
            }
        )
        assertTrue(
            lowConfidenceConflicts.isNotEmpty()
        )
    }
}