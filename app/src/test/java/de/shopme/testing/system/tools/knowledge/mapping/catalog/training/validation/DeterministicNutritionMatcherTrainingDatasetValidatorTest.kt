package de.shopme.testing.system.tools.knowledge.mapping.catalog.training.validation

import com.google.gson.GsonBuilder
import de.shopme.tools.knowledge.mapping.catalog.training.NutritionMatcherTrainingDataset
import de.shopme.tools.knowledge.mapping.catalog.training.NutritionMatcherTrainingDatasetSummary
import de.shopme.tools.knowledge.mapping.catalog.training.NutritionMatcherTrainingExample
import de.shopme.tools.knowledge.mapping.catalog.training.NutritionMatcherTrainingExampleRole
import de.shopme.tools.knowledge.mapping.catalog.training.NutritionMatcherTrainingLabel
import de.shopme.tools.knowledge.mapping.catalog.training.NutritionMatcherTrainingProvenance
import de.shopme.tools.knowledge.mapping.catalog.training.validation.DeterministicNutritionMatcherTrainingDatasetValidator
import de.shopme.tools.knowledge.mapping.catalog.training.validation.NutritionMatcherTrainingDatasetValidationIssueCode
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeterministicNutritionMatcherTrainingDatasetValidatorTest {

    @Test
    fun acceptValidDeterministicDataset() {

        val directory =
            createTempDirectory(
                prefix =
                    "valid-nutrition-matcher-dataset-"
            )
                .toFile()

        try {
            val example =
                validPositiveExample()

            val datasetFile =
                writeDataset(
                    directory =
                        directory,
                    dataset =
                        NutritionMatcherTrainingDataset(
                            summary =
                                NutritionMatcherTrainingDatasetSummary(
                                    sourceCatalogKeyCount = 1,
                                    exampleCount = 1,
                                    positiveCount = 1,
                                    negativeCount = 0,
                                    acceptedSelectedCount = 1,
                                    rejectedSelectedCount = 0,
                                    rejectedNoMatchCandidateCount = 0,
                                    nonSelectedAlternativeCount = 0,
                                    acceptedOriginalMatchCount =
                                        0,
                                ),
                            examples =
                                listOf(
                                    example
                                )
                        )
                )

            val result =
                DeterministicNutritionMatcherTrainingDatasetValidator()
                    .validate(
                        datasetFile =
                            datasetFile,
                        output =
                            PrintStream(
                                ByteArrayOutputStream()
                            )
                    )

            assertTrue(
                result.valid
            )

            assertEquals(
                expected = 0,
                actual =
                    result.issueCount
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun reportInvalidLabelSummaryAndStableId() {

        val directory =
            createTempDirectory(
                prefix =
                    "invalid-nutrition-matcher-dataset-"
            )
                .toFile()

        try {
            val valid =
                validPositiveExample()

            val invalid =
                valid.copy(
                    id =
                        "invalid-id",
                    label =
                        NutritionMatcherTrainingLabel.NEGATIVE,
                    selected =
                        false
                )

            val datasetFile =
                writeDataset(
                    directory =
                        directory,
                    dataset =
                        NutritionMatcherTrainingDataset(
                            summary =
                                NutritionMatcherTrainingDatasetSummary(
                                    sourceCatalogKeyCount = 1,
                                    exampleCount = 2,
                                    positiveCount = 2,
                                    negativeCount = 0,
                                    acceptedSelectedCount = 1,
                                    rejectedSelectedCount = 0,
                                    rejectedNoMatchCandidateCount = 0,
                                    nonSelectedAlternativeCount = 0,
                                    acceptedOriginalMatchCount =
                                        0,
                                ),
                            examples =
                                listOf(
                                    invalid
                                )
                        )
                )

            val result =
                DeterministicNutritionMatcherTrainingDatasetValidator()
                    .validate(
                        datasetFile =
                            datasetFile,
                        output =
                            PrintStream(
                                ByteArrayOutputStream()
                            )
                    )

            assertFalse(
                result.valid
            )

            val issueCodes =
                result.issues.map {
                    it.code
                }
                    .toSet()

            assertTrue(
                NutritionMatcherTrainingDatasetValidationIssueCode
                    .INVALID_STABLE_ID in issueCodes
            )

            assertTrue(
                NutritionMatcherTrainingDatasetValidationIssueCode
                    .INVALID_POSITIVE_ROLE in issueCodes
            )

            assertTrue(
                NutritionMatcherTrainingDatasetValidationIssueCode
                    .INVALID_SELECTED_STATE in issueCodes
            )

            assertTrue(
                NutritionMatcherTrainingDatasetValidationIssueCode
                    .SUMMARY_EXAMPLE_COUNT_MISMATCH in issueCodes
            )

            assertTrue(
                NutritionMatcherTrainingDatasetValidationIssueCode
                    .SUMMARY_POSITIVE_COUNT_MISMATCH in issueCodes
            )

            assertTrue(
                NutritionMatcherTrainingDatasetValidationIssueCode
                    .SUMMARY_NEGATIVE_COUNT_MISMATCH in issueCodes
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    private fun validPositiveExample():
            NutritionMatcherTrainingExample {

        val catalogKey =
            "fruit yogurt"

        val serverKey =
            "cherry fruit yogurt"

        val label =
            NutritionMatcherTrainingLabel.POSITIVE

        val role =
            NutritionMatcherTrainingExampleRole
                .ACCEPTED_SELECTED

        return NutritionMatcherTrainingExample(
            id =
                createStableId(
                    catalogKey =
                        catalogKey,
                    serverKey =
                        serverKey,
                    label =
                        label,
                    role =
                        role
                ),
            catalogKey =
                catalogKey,
            serverArtifact =
                "nutrition.json",
            serverKey =
                serverKey,
            label =
                label,
            role =
                role,
            selected =
                true,
            candidateRank =
                1,
            candidateCount =
                2,
            diagnosticScore =
                0.79,
            diagnosticScoreAvailable =
                true,
            sharedTokens =
                listOf(
                    "fruit",
                    "yogurt"
                ),
            matcherConfidence =
                0.78,
            originalDecisionType =
                "MATCH",
            originalDecisionReason =
                "Representative yogurt.",
            originalValidationStatus =
                "REJECTED_LOW_CONFIDENCE",
            originalValidationReason =
                "Below threshold.",
            representativeDecisionType =
                "REPRESENTATIVE",
            representativeReasons =
                listOf(
                    "SAME_PRODUCT_CLASS"
                ),
            trainingWeight =
                1.0,
            provenance =
                NutritionMatcherTrainingProvenance(
                    sourceType =
                        "GPT_5_5_MATCHER_WITH_DETERMINISTIC_VALIDATION",
                    candidateQualityFile =
                        "nutrition.rejected-candidate-quality.json",
                    diagnosticsFile =
                        "nutrition.match-diagnostics.json",
                    representativeValidationFile =
                        "nutrition.low-confidence-validation.json",
                    sourceVersion =
                        1,
                    matcher =
                        "GPT-5.5 CatalogToServerMatcher",
                    validator =
                        "DeterministicRepresentativeNutritionMappingValidator"
                )
        )
    }

    private fun writeDataset(
        directory: File,
        dataset: NutritionMatcherTrainingDataset
    ): File {

        val file =
            File(
                directory,
                "nutrition.matcher-training-dataset.json"
            )

        val gson =
            GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()

        file.writeText(
            gson.toJson(dataset) + "\n"
        )

        return file
    }

    private fun createStableId(
        catalogKey: String,
        serverKey: String,
        label: NutritionMatcherTrainingLabel,
        role: NutritionMatcherTrainingExampleRole
    ): String {

        val canonicalValue =
            listOf(
                "nutrition-catalog-server-matcher-v1",
                catalogKey,
                "nutrition.json",
                serverKey,
                label.name,
                role.name
            )
                .joinToString("|")

        val digest =
            MessageDigest
                .getInstance("SHA-256")
                .digest(
                    canonicalValue.toByteArray(
                        StandardCharsets.UTF_8
                    )
                )

        return digest.joinToString("") {
            "%02x".format(
                it.toInt() and 0xff
            )
        }
    }
}