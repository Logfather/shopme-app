package de.shopme.testing.system.tools.knowledge.mapping.catalog.training.model

import de.shopme.tools.knowledge.mapping.catalog.training.NutritionDomainMismatchFeatures
import de.shopme.tools.knowledge.mapping.catalog.training.NutritionMatcherTrainingExample
import de.shopme.tools.knowledge.mapping.catalog.training.NutritionMatcherTrainingExampleRole
import de.shopme.tools.knowledge.mapping.catalog.training.NutritionMatcherTrainingLabel
import de.shopme.tools.knowledge.mapping.catalog.training.NutritionMatcherTrainingProvenance
import de.shopme.tools.knowledge.mapping.catalog.training.model.LocalNutritionMatcherCandidate
import de.shopme.tools.knowledge.mapping.catalog.training.model.LocalNutritionMatcherFeatureExtractor
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class LocalNutritionMatcherFeatureExtractorLeakageTest {

    @Test
    fun doNotExposeDiagnosticScoreAvailabilityAsFeature() {

        val extractor =
            LocalNutritionMatcherFeatureExtractor()

        assertFalse(
            "diagnostic_score_available" in
                    extractor.featureNames,
        )

        assertEquals(
            expected =
                LocalNutritionMatcherFeatureExtractor.FEATURE_COUNT,
            actual =
                extractor.featureNames.size,
        )
    }

    @Test
    fun imputeUnavailableDiagnosticScoresNeutrally() {

        val extractor =
            LocalNutritionMatcherFeatureExtractor()

        val imputationValue =
            0.6842

        val unavailableWithZeroPlaceholder =
            example(
                diagnosticScore =
                    0.0,
                diagnosticScoreAvailable =
                    false,
            )

        val unavailableWithDifferentPlaceholder =
            example(
                diagnosticScore =
                    999.0,
                diagnosticScoreAvailable =
                    false,
            )

        val availableAtImputationValue =
            example(
                diagnosticScore =
                    imputationValue,
                diagnosticScoreAvailable =
                    true,
            )

        val zeroPlaceholderFeatures =
            extractor.extract(
                example =
                    unavailableWithZeroPlaceholder,
                diagnosticScoreImputationValue =
                    imputationValue,
            )

        val differentPlaceholderFeatures =
            extractor.extract(
                example =
                    unavailableWithDifferentPlaceholder,
                diagnosticScoreImputationValue =
                    imputationValue,
            )

        val availableFeatures =
            extractor.extract(
                example =
                    availableAtImputationValue,
                diagnosticScoreImputationValue =
                    imputationValue,
            )

        assertContentEquals(
            expected =
                zeroPlaceholderFeatures,
            actual =
                differentPlaceholderFeatures,
        )

        assertContentEquals(
            expected =
                availableFeatures,
            actual =
                zeroPlaceholderFeatures,
        )
    }

    @Test
    fun includeDomainMismatchCountsWithoutAvailabilityLeakage() {

        val extractor =
            LocalNutritionMatcherFeatureExtractor()

        assertEquals(
            expected =
                LocalNutritionMatcherFeatureExtractor.FEATURE_COUNT,
            actual =
                extractor.featureNames.size,
        )

        assertEquals(
            expected =
                LocalNutritionMatcherFeatureExtractor
                    .BASE_FEATURE_NAMES,
            actual =
                extractor.featureNames.take(
                    LocalNutritionMatcherFeatureExtractor
                        .BASE_FEATURE_COUNT,
                ),
        )

        assertEquals(
            expected =
                LocalNutritionMatcherFeatureExtractor
                    .DOMAIN_MISMATCH_FEATURE_NAMES,
            actual =
                extractor.featureNames.drop(
                    LocalNutritionMatcherFeatureExtractor
                        .BASE_FEATURE_COUNT,
                ),
        )

        assertFalse(
            "diagnostic_score_available" in
                    extractor.featureNames,
        )

        assertFalse(
            "domain_feature_version" in
                    extractor.featureNames,
        )

        assertFalse(
            "domain_report_relationship_present" in
                    extractor.featureNames,
        )
    }

    @Test
    fun appendDomainMismatchFeaturesInDeterministicOrder() {

        val extractor =
            LocalNutritionMatcherFeatureExtractor()

        val candidate =
            LocalNutritionMatcherCandidate(
                catalogKey =
                    "vegetarian wrap",
                serverKey =
                    "vegetarian lettuce wrap",
                candidateRank =
                    1,
                candidateCount =
                    5,
                diagnosticScore =
                    0.82,
                diagnosticScoreAvailable =
                    true,
                sharedTokens =
                    listOf(
                        "vegetarian",
                        "wrap",
                    ),
                domainMismatchFeatures =
                    NutritionDomainMismatchFeatures(
                        version = 1,
                        reportRelationshipPresent = true,
                        observationCount = 14,
                        dietOrSubstituteDifferenceCount = 1,
                        crossDomainMismatchCount = 2,
                        sameDomainDifferentEntityCount = 3,
                        formOrProcessingDifferenceCount = 4,
                        regionOrStyleDifferenceCount = 5,
                        compatibleDomainRelationshipCount = 6,
                        unknownTokenInvolvedCount = 7,
                        nonSemanticTokenDifferenceCount = 8,
                        unknownMismatchCount = 9,
                        identityConflictCount = 10,
                        modifierDifferenceCount = 11,
                        knownSemanticObservationCount = 12,
                        unknownSemanticObservationCount = 2,
                    ),
            )

        val features =
            extractor.extract(
                candidate =
                    candidate,
                diagnosticScoreImputationValue =
                    0.5,
            )

        assertEquals(
            expected =
                LocalNutritionMatcherFeatureExtractor.FEATURE_COUNT,
            actual =
                features.size,
        )

        assertContentEquals(
            expected =
                doubleArrayOf(
                    14.0,
                    1.0,
                    2.0,
                    3.0,
                    4.0,
                    5.0,
                    6.0,
                    7.0,
                    8.0,
                    9.0,
                    10.0,
                    11.0,
                    12.0,
                    2.0,
                ),
            actual =
                features.copyOfRange(
                    fromIndex =
                        LocalNutritionMatcherFeatureExtractor
                            .BASE_FEATURE_COUNT,
                    toIndex =
                        LocalNutritionMatcherFeatureExtractor
                            .FEATURE_COUNT,
                ),
        )
    }

    @Test
    fun useNeutralDomainMismatchVectorWhenFeaturesAreUnavailable() {

        val extractor =
            LocalNutritionMatcherFeatureExtractor()

        val candidate =
            LocalNutritionMatcherCandidate(
                catalogKey =
                    "apple",
                serverKey =
                    "fresh apple",
                candidateRank =
                    1,
                candidateCount =
                    5,
                diagnosticScore =
                    0.8,
                diagnosticScoreAvailable =
                    true,
                sharedTokens =
                    listOf("apple"),
                domainMismatchFeatures =
                    null,
            )

        val features =
            extractor.extract(
                candidate =
                    candidate,
                diagnosticScoreImputationValue =
                    0.5,
            )

        assertContentEquals(
            expected =
                DoubleArray(
                    LocalNutritionMatcherFeatureExtractor
                        .DOMAIN_MISMATCH_FEATURE_COUNT,
                ),
            actual =
                features.copyOfRange(
                    fromIndex =
                        LocalNutritionMatcherFeatureExtractor
                            .BASE_FEATURE_COUNT,
                    toIndex =
                        LocalNutritionMatcherFeatureExtractor
                            .FEATURE_COUNT,
                ),
        )
    }

    @Test
    fun reportRelationshipPresenceDoesNotChangeFeatureVector() {

        val extractor =
            LocalNutritionMatcherFeatureExtractor()

        val withoutReport =
            NutritionDomainMismatchFeatures(
                version = 1,
                reportRelationshipPresent = false,
                observationCount = 2,
                modifierDifferenceCount = 1,
                knownSemanticObservationCount = 2,
            )

        val withReport =
            withoutReport.copy(
                reportRelationshipPresent = true,
            )

        fun candidate(
            features: NutritionDomainMismatchFeatures,
        ): LocalNutritionMatcherCandidate {

            return LocalNutritionMatcherCandidate(
                catalogKey =
                    "apple juice",
                serverKey =
                    "organic apple juice",
                candidateRank =
                    1,
                candidateCount =
                    5,
                diagnosticScore =
                    0.8,
                diagnosticScoreAvailable =
                    true,
                sharedTokens =
                    listOf(
                        "apple",
                        "juice",
                    ),
                domainMismatchFeatures =
                    features,
            )
        }

        val first =
            extractor.extract(
                candidate =
                    candidate(withoutReport),
                diagnosticScoreImputationValue =
                    0.5,
            )

        val second =
            extractor.extract(
                candidate =
                    candidate(withReport),
                diagnosticScoreImputationValue =
                    0.5,
            )

        assertContentEquals(
            expected =
                first,
            actual =
                second,
        )
    }

    private fun example(
        diagnosticScore: Double,
        diagnosticScoreAvailable: Boolean,
    ): NutritionMatcherTrainingExample {

        return NutritionMatcherTrainingExample(
            id =
                "fixture",
            catalogKey =
                "fruit yogurt",
            serverArtifact =
                "nutrition.json",
            serverKey =
                "cherry fruit yogurt",
            label =
                NutritionMatcherTrainingLabel.POSITIVE,
            role =
                NutritionMatcherTrainingExampleRole
                    .ACCEPTED_ORIGINAL_MATCH,
            selected =
                true,
            candidateRank =
                1,
            candidateCount =
                5,
            diagnosticScore =
                diagnosticScore,
            diagnosticScoreAvailable =
                diagnosticScoreAvailable,
            sharedTokens =
                listOf(
                    "fruit",
                    "yogurt",
                ),
            domainMismatchFeatures =
                NutritionDomainMismatchFeatures(
                    version = 1,
                    reportRelationshipPresent = false,
                ),
            matcherConfidence =
                0.96,
            originalDecisionType =
                "MATCH",
            originalDecisionReason =
                "Accepted match.",
            originalValidationStatus =
                "ACCEPTED",
            originalValidationReason =
                "Accepted.",
            representativeDecisionType =
                null,
            representativeReasons =
                emptyList(),
            trainingWeight =
                1.0,
            provenance =
                NutritionMatcherTrainingProvenance(
                    sourceType =
                        "TEST",
                    candidateQualityFile =
                        "candidate-quality.json",
                    diagnosticsFile =
                        "diagnostics.json",
                    representativeValidationFile =
                        "validation.json",
                    sourceVersion =
                        1,
                    matcher =
                        "test matcher",
                    validator =
                        "test validator",
                ),
        )
    }
}