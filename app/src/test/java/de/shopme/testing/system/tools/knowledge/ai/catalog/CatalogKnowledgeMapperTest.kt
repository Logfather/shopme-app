package de.shopme.testing.system.tools.knowledge.ai.catalog

import de.shopme.tools.knowledge.ai.catalog.CatalogKnowledgeMapper
import de.shopme.tools.knowledge.ki_candidates.CandidateFoodKnowledgePatch
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import org.junit.Assert
import org.junit.Test

class CatalogKnowledgeMapperTest {

    private val mapper =
        CatalogKnowledgeMapper()

    @Test
    fun mapsCandidateDimensionsToCatalogKnowledgeReferences() {

        val patch =
            CandidateFoodKnowledgePatch(
                canonicalId = "00000758",
                aliases = setOf("Test Food"),
                dimensions = listOf(
                    testDimension(KnowledgeDimensionCandidateType.NUTRITION),
                    testDimension(KnowledgeDimensionCandidateType.INGREDIENTS),
                    testDimension(KnowledgeDimensionCandidateType.TAXONOMY)
                ),
                metadata = CandidateMetadata(
                    "Open Food Facts",
                    "00000758",
                    1.0,
                    "test"
                )
            )

        val result =
            mapper.map(patch)

        Assert.assertEquals("00000758", result.nutrition?.reference)
        Assert.assertEquals("Open Food Facts", result.nutrition?.source)

        Assert.assertEquals("00000758", result.ingredients?.reference)
        Assert.assertEquals("Open Food Facts", result.ingredients?.source)

        Assert.assertEquals("00000758", result.taxonomy?.reference)
        Assert.assertEquals("Open Food Facts", result.taxonomy?.source)
    }

    @Test
    fun leavesMissingDimensionsEmpty() {

        val patch =
            CandidateFoodKnowledgePatch(
                canonicalId = "0000101209159",
                aliases = setOf("Test Food"),
                dimensions = listOf(
                    testDimension(KnowledgeDimensionCandidateType.INGREDIENTS)
                ),
                metadata = CandidateMetadata(
                    "Open Food Facts",
                    "0000101209159",
                    1.0,
                    "test"
                )
            )

        val result =
            mapper.map(patch)

        Assert.assertNull(result.nutrition)
        Assert.assertEquals("0000101209159", result.ingredients?.reference)
        Assert.assertNull(result.taxonomy)
    }

    @Test
    fun mapsRemainingCandidateDimensionsToCatalogKnowledgeReferences() {

        val patch =
            CandidateFoodKnowledgePatch(
                canonicalId = "00000758",
                aliases = setOf("Test Food"),
                dimensions = listOf(
                    testDimension(KnowledgeDimensionCandidateType.ALLERGENS),
                    testDimension(KnowledgeDimensionCandidateType.GLYCEMIC),
                    testDimension(KnowledgeDimensionCandidateType.CARBON),
                    testDimension(KnowledgeDimensionCandidateType.WATER),
                    testDimension(KnowledgeDimensionCandidateType.WATER_STRESS),
                    testDimension(KnowledgeDimensionCandidateType.SEASONALITY),
                    testDimension(KnowledgeDimensionCandidateType.PACKAGING),
                    testDimension(KnowledgeDimensionCandidateType.FAIRTRADE),
                    testDimension(KnowledgeDimensionCandidateType.ANIMAL_WELFARE),
                    testDimension(KnowledgeDimensionCandidateType.BIODIVERSITY),
                    testDimension(KnowledgeDimensionCandidateType.POLLINATOR),
                    testDimension(KnowledgeDimensionCandidateType.LOCALITY),
                    testDimension(KnowledgeDimensionCandidateType.FOOD_MILES),
                    testDimension(KnowledgeDimensionCandidateType.PRODUCTION)
                ),
                metadata = CandidateMetadata(
                    "Open Food Facts",
                    "00000758",
                    1.0,
                    "test"
                )
            )

        val result =
            mapper.map(patch)

        Assert.assertEquals("00000758", result.allergens?.reference)
        Assert.assertEquals("00000758", result.glycemicIndex?.reference)
        Assert.assertEquals("00000758", result.carbon?.reference)
        Assert.assertEquals("00000758", result.water?.reference)
        Assert.assertEquals("00000758", result.waterStress?.reference)
        Assert.assertEquals("00000758", result.seasonality?.reference)
        Assert.assertEquals("00000758", result.packaging?.reference)
        Assert.assertEquals("00000758", result.fairTrade?.reference)
        Assert.assertEquals("00000758", result.animalWelfare?.reference)
        Assert.assertEquals("00000758", result.biodiversity?.reference)
        Assert.assertEquals("00000758", result.pollinator?.reference)
        Assert.assertEquals("00000758", result.locality?.reference)
        Assert.assertEquals("00000758", result.foodMiles?.reference)
        Assert.assertEquals("00000758", result.production?.reference)

        Assert.assertEquals("Open Food Facts", result.allergens?.source)
        Assert.assertEquals("Open Food Facts", result.glycemicIndex?.source)
        Assert.assertEquals("Open Food Facts", result.carbon?.source)
        Assert.assertEquals("Open Food Facts", result.water?.source)
        Assert.assertEquals("Open Food Facts", result.waterStress?.source)
        Assert.assertEquals("Open Food Facts", result.seasonality?.source)
        Assert.assertEquals("Open Food Facts", result.packaging?.source)
        Assert.assertEquals("Open Food Facts", result.fairTrade?.source)
        Assert.assertEquals("Open Food Facts", result.animalWelfare?.source)
        Assert.assertEquals("Open Food Facts", result.biodiversity?.source)
        Assert.assertEquals("Open Food Facts", result.pollinator?.source)
        Assert.assertEquals("Open Food Facts", result.locality?.source)
        Assert.assertEquals("Open Food Facts", result.foodMiles?.source)
        Assert.assertEquals("Open Food Facts", result.production?.source)
    }

    private fun testDimension(
        type: KnowledgeDimensionCandidateType
    ): KnowledgeDimensionCandidate =
        KnowledgeDimensionCandidate(
            dimension = type,
            payload = Unit
        )
}