package de.shopme.testing.system.tools.knowledge.mapping.catalog.training

import com.google.gson.JsonParser
import de.shopme.testing.system.tools.knowledge.nutrition.training.NutritionMatcherTrainingDatasetDomainFeatureEnricher
import de.shopme.tools.knowledge.mapping.catalog.training.NutritionMatcherTrainingDatasetExporter
import de.shopme.tools.knowledge.mapping.catalog.training.NutritionMatcherTrainingExampleRole
import de.shopme.tools.knowledge.mapping.catalog.training.NutritionMatcherTrainingLabel
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunNutritionMatcherTrainingDatasetExportTest {

    @Test
    fun exportPositiveAndNegativeNutritionMatcherDataset() {

        val projectRoot =
            File("..")

        val reportDirectory =
            File(
                projectRoot,
                "data/generated/knowledge/reports",
            )

        val mappingFile =
            File(
                projectRoot,
                "data/generated/knowledge/" +
                        "mappings/" +
                        "catalog-server.mappings.json",
            )

        val trainingDirectory =
            File(
                projectRoot,
                "data/generated/knowledge/training",
            )

        val candidateQualityFile =
            File(
                reportDirectory,
                "nutrition.rejected-candidate-quality.json",
            )

        val diagnosticsFile =
            File(
                reportDirectory,
                "nutrition.match-diagnostics.json",
            )

        val representativeValidationFile =
            File(
                reportDirectory,
                "nutrition.low-confidence-validation.json",
            )

        val foodDomainMismatchReportFile =
            File(
                reportDirectory,
                "nutrition.food-domain-mismatches.json",
            )

        val outputFile =
            File(
                trainingDirectory,
                "nutrition.matcher-training-dataset.json",
            )

        require(candidateQualityFile.isFile) {
            "Rejected nutrition candidate quality file " +
                    "does not exist: " +
                    candidateQualityFile.absolutePath
        }

        require(diagnosticsFile.isFile) {
            "Nutrition match diagnostics file does not exist: " +
                    diagnosticsFile.absolutePath
        }

        require(representativeValidationFile.isFile) {
            "Representative nutrition validation file " +
                    "does not exist: " +
                    representativeValidationFile.absolutePath
        }

        require(mappingFile.isFile) {
            "Catalog-server mapping file does not exist: " +
                    mappingFile.absolutePath
        }

        require(foodDomainMismatchReportFile.isFile) {
            "Nutrition Food-Domain mismatch report does not exist: " +
                    foodDomainMismatchReportFile.absolutePath
        }

        /*
         * Step 1:
         * Export the original positive and negative matcher dataset.
         *
         * This call writes outputFile. Domain-Mismatch enrichment must
         * therefore happen strictly after this exporter has completed.
         */
        val result =
            NutritionMatcherTrainingDatasetExporter()
                .run(
                    candidateQualityFile =
                        candidateQualityFile,
                    diagnosticsFile =
                        diagnosticsFile,
                    representativeValidationFile =
                        representativeValidationFile,
                    outputFile =
                        outputFile,
                    mappingFile =
                        mappingFile,
                )

        val dataset =
            result.dataset

        assertTrue(
            outputFile.isFile,
            "Nutrition matcher training dataset was not created: " +
                    outputFile.absolutePath,
        )

        assertTrue(
            dataset.summary.sourceCatalogKeyCount > 0,
        )

        assertTrue(
            dataset.summary.exampleCount > 0,
        )

        assertTrue(
            dataset.summary.positiveCount > 0,
        )

        assertTrue(
            dataset.summary.negativeCount > 0,
        )

        assertEquals(
            expected =
                dataset.summary.exampleCount,
            actual =
                dataset.examples.size,
        )

        assertEquals(
            expected =
                dataset.summary.exampleCount,
            actual =
                dataset.summary.positiveCount +
                        dataset.summary.negativeCount,
        )

        assertTrue(
            dataset.examples
                .filter {
                    it.label ==
                            NutritionMatcherTrainingLabel.POSITIVE
                }
                .all {
                    it.role ==
                            NutritionMatcherTrainingExampleRole
                                .ACCEPTED_ORIGINAL_MATCH ||
                            it.role ==
                            NutritionMatcherTrainingExampleRole
                                .ACCEPTED_SELECTED
                },
        )

        assertTrue(
            dataset.examples
                .filter {
                    it.label ==
                            NutritionMatcherTrainingLabel.POSITIVE
                }
                .all {
                    it.selected
                },
        )

        assertTrue(
            dataset.examples
                .filter {
                    it.label ==
                            NutritionMatcherTrainingLabel.NEGATIVE
                }
                .all {
                    it.role !=
                            NutritionMatcherTrainingExampleRole
                                .ACCEPTED_ORIGINAL_MATCH &&
                            it.role !=
                            NutritionMatcherTrainingExampleRole
                                .ACCEPTED_SELECTED
                },
        )

        assertTrue(
            dataset.examples
                .filter {
                    it.role ==
                            NutritionMatcherTrainingExampleRole
                                .ACCEPTED_ORIGINAL_MATCH
                }
                .all {
                    it.label ==
                            NutritionMatcherTrainingLabel.POSITIVE &&
                            it.selected
                },
        )

        assertTrue(
            dataset.examples
                .filter {
                    it.role ==
                            NutritionMatcherTrainingExampleRole
                                .ACCEPTED_SELECTED
                }
                .all {
                    it.label ==
                            NutritionMatcherTrainingLabel.POSITIVE &&
                            it.selected
                },
        )

        assertTrue(
            dataset.examples.all {
                it.id.length == 64
            },
        )

        assertTrue(
            dataset.examples.all {
                it.trainingWeight > 0.0 &&
                        it.trainingWeight <= 1.0
            },
        )

        val originallyPersisted =
            JsonParser.parseString(
                outputFile.readText(),
            )
                .asJsonObject

        assertEquals(
            expected = 1,
            actual =
                originallyPersisted["version"]
                    .asInt,
        )

        assertEquals(
            expected =
                "NUTRITION_CATALOG_SERVER_MATCHER",
            actual =
                originallyPersisted["datasetType"]
                    .asString,
        )

        assertEquals(
            expected =
                dataset.summary.exampleCount,
            actual =
                originallyPersisted["examples"]
                    .asJsonArray
                    .size(),
        )

        assertTrue(
            dataset.summary.acceptedOriginalMatchCount > 0,
            "Training dataset must contain originally accepted matches.",
        )

        assertEquals(
            expected =
                dataset.summary.positiveCount,
            actual =
                dataset.summary.acceptedOriginalMatchCount +
                        dataset.summary.acceptedSelectedCount,
        )

        assertTrue(
            dataset.summary.acceptedSelectedCount > 0,
            "Training dataset must contain representative accepted matches.",
        )

        assertEquals(
            expected = 440,
            actual =
                dataset.summary.acceptedOriginalMatchCount,
        )

        assertEquals(
            expected = 37,
            actual =
                dataset.summary.acceptedSelectedCount,
        )

        assertEquals(
            expected = 477,
            actual =
                dataset.summary.positiveCount,
        )

        assertEquals(
            expected = 3779,
            actual =
                dataset.summary.negativeCount,
        )

        assertEquals(
            expected = 4256,
            actual =
                dataset.summary.exampleCount,
        )

        println()
        println(
            "Nutrition matcher catalog keys=" +
                    dataset.summary.sourceCatalogKeyCount,
        )
        println(
            "Nutrition matcher examples=" +
                    dataset.summary.exampleCount,
        )
        println(
            "Positive examples=" +
                    dataset.summary.positiveCount,
        )
        println(
            "Negative examples=" +
                    dataset.summary.negativeCount,
        )
        println(
            "Rejected selected=" +
                    dataset.summary.rejectedSelectedCount,
        )
        println(
            "NO_MATCH candidates=" +
                    dataset.summary
                        .rejectedNoMatchCandidateCount,
        )
        println(
            "Alternative negatives=" +
                    dataset.summary
                        .nonSelectedAlternativeCount,
        )
        println(
            "Training dataset=" +
                    outputFile.path,
        )
        println(
            "Accepted original matches=" +
                    dataset.summary.acceptedOriginalMatchCount,
        )

        /*
         * Step 2:
         * Enrich the already persisted dataset with deterministic
         * Domain-Mismatch features.
         *
         * No exporter or writer may write outputFile after this point.
         */
        val domainMismatchFeatureResult =
            NutritionMatcherTrainingDatasetDomainFeatureEnricher()
                .enrich(
                    datasetFile =
                        outputFile,
                    mismatchReportFile =
                        foodDomainMismatchReportFile,
                    outputFile =
                        outputFile,
                )

        check(
            domainMismatchFeatureResult.datasetExampleCount ==
                    domainMismatchFeatureResult.enrichedExampleCount,
        ) {
            "Nutrition Domain-Mismatch features were not exported " +
                    "for all training examples: datasetExamples=" +
                    "${domainMismatchFeatureResult.datasetExampleCount}, " +
                    "enrichedExamples=" +
                    domainMismatchFeatureResult.enrichedExampleCount
        }

        check(
            domainMismatchFeatureResult.matchedRelationshipCount +
                    domainMismatchFeatureResult.unmatchedRelationshipCount ==
                    domainMismatchFeatureResult.datasetExampleCount,
        ) {
            "Nutrition Domain-Mismatch feature coverage is " +
                    "inconsistent: matched=" +
                    "${domainMismatchFeatureResult.matchedRelationshipCount}, " +
                    "unmatched=" +
                    "${domainMismatchFeatureResult.unmatchedRelationshipCount}, " +
                    "datasetExamples=" +
                    domainMismatchFeatureResult.datasetExampleCount
        }

        /*
         * Step 3:
         * Read the final persisted file again.
         *
         * These checks validate the actual artifact on disk, not merely
         * the in-memory enrichment result.
         */
        val enrichedDatasetRoot =
            JsonParser.parseString(
                outputFile.readText(),
            )
                .asJsonObject

        assertEquals(
            expected = 1,
            actual =
                enrichedDatasetRoot[
                    "domainMismatchFeatureVersion"
                ].asInt,
        )

        val domainMismatchFeatureCoverage =
            enrichedDatasetRoot[
                "domainMismatchFeatureCoverage"
            ].asJsonObject

        assertEquals(
            expected =
                domainMismatchFeatureResult.datasetExampleCount,
            actual =
                domainMismatchFeatureCoverage[
                    "exampleCount"
                ].asInt,
        )

        assertEquals(
            expected =
                domainMismatchFeatureResult
                    .matchedRelationshipCount,
            actual =
                domainMismatchFeatureCoverage[
                    "matchedRelationshipCount"
                ].asInt,
        )

        assertEquals(
            expected =
                domainMismatchFeatureResult
                    .unmatchedRelationshipCount,
            actual =
                domainMismatchFeatureCoverage[
                    "unmatchedRelationshipCount"
                ].asInt,
        )

        val persistedExamples =
            enrichedDatasetRoot[
                "examples"
            ].asJsonArray

        assertEquals(
            expected =
                dataset.summary.exampleCount,
            actual =
                persistedExamples.size(),
        )

        val persistedExamplesWithFeatures =
            persistedExamples.count { exampleElement ->
                exampleElement
                    .asJsonObject
                    .has(
                        "domainMismatchFeatures",
                    )
            }

        assertEquals(
            expected =
                dataset.summary.exampleCount,
            actual =
                persistedExamplesWithFeatures,
        )

        val persistedMatchedRelationships =
            persistedExamples.count { exampleElement ->
                exampleElement
                    .asJsonObject[
                    "domainMismatchFeatures"
                ]
                    .asJsonObject[
                    "reportRelationshipPresent"
                ]
                    .asBoolean
            }

        val persistedUnmatchedRelationships =
            persistedExamples.count { exampleElement ->
                !exampleElement
                    .asJsonObject[
                    "domainMismatchFeatures"
                ]
                    .asJsonObject[
                    "reportRelationshipPresent"
                ]
                    .asBoolean
            }

        assertEquals(
            expected =
                domainMismatchFeatureResult
                    .matchedRelationshipCount,
            actual =
                persistedMatchedRelationships,
        )

        assertEquals(
            expected =
                domainMismatchFeatureResult
                    .unmatchedRelationshipCount,
            actual =
                persistedUnmatchedRelationships,
        )

        assertEquals(
            expected =
                dataset.summary.exampleCount,
            actual =
                persistedMatchedRelationships +
                        persistedUnmatchedRelationships,
        )

        /*
         * The enrichment must not alter the original training labels or
         * the number of examples.
         */
        val persistedPositiveCount =
            persistedExamples.count { exampleElement ->
                exampleElement
                    .asJsonObject["label"]
                    .asString ==
                        NutritionMatcherTrainingLabel
                            .POSITIVE
                            .name
            }

        val persistedNegativeCount =
            persistedExamples.count { exampleElement ->
                exampleElement
                    .asJsonObject["label"]
                    .asString ==
                        NutritionMatcherTrainingLabel
                            .NEGATIVE
                            .name
            }

        assertEquals(
            expected =
                dataset.summary.positiveCount,
            actual =
                persistedPositiveCount,
        )

        assertEquals(
            expected =
                dataset.summary.negativeCount,
            actual =
                persistedNegativeCount,
        )

        println()
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        )
        println(
            "NUTRITION DOMAIN-MISMATCH FEATURE EXPORT",
        )
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        )
        println(
            "Domain-Mismatch feature version : 1",
        )
        println(
            "Enriched examples               : " +
                    domainMismatchFeatureResult.enrichedExampleCount,
        )
        println(
            "Matched relationships           : " +
                    domainMismatchFeatureResult
                        .matchedRelationshipCount,
        )
        println(
            "Unmatched relationships         : " +
                    domainMismatchFeatureResult
                        .unmatchedRelationshipCount,
        )
        println(
            "Persisted examples with features: " +
                    persistedExamplesWithFeatures,
        )
        println(
            "Persisted positive examples     : " +
                    persistedPositiveCount,
        )
        println(
            "Persisted negative examples     : " +
                    persistedNegativeCount,
        )
        println(
            "Output                          : " +
                    outputFile.path,
        )
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        )
    }
}