package de.shopme.tools.knowledge.mapping.catalog.training.validation

import com.google.gson.Gson
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMappingValidationStatus
import de.shopme.tools.knowledge.mapping.catalog.training.NutritionMatcherTrainingDataset
import de.shopme.tools.knowledge.mapping.catalog.training.NutritionMatcherTrainingExample
import de.shopme.tools.knowledge.mapping.catalog.training.NutritionMatcherTrainingExampleRole
import de.shopme.tools.knowledge.mapping.catalog.training.NutritionMatcherTrainingLabel
import java.io.File
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class DeterministicNutritionMatcherTrainingDatasetValidator {

    fun validate(
        datasetFile: File,
        output: PrintStream = System.out
    ): NutritionMatcherTrainingDatasetValidationResult {

        require(datasetFile.isFile) {
            "Nutrition matcher training dataset does not exist: " +
                    datasetFile.absolutePath
        }

        val dataset =
            readDataset(
                datasetFile = datasetFile
            )

        val issues =
            mutableListOf<
                    NutritionMatcherTrainingDatasetValidationIssue
                    >()

        validateDatasetMetadata(
            dataset = dataset,
            issues = issues
        )

        validateSummary(
            dataset = dataset,
            issues = issues
        )

        validateUniqueness(
            examples = dataset.examples,
            issues = issues
        )

        validateDeterministicOrder(
            examples = dataset.examples,
            issues = issues
        )

        dataset.examples.forEach { example ->

            validateExample(
                example = example,
                issues = issues
            )
        }

        val sortedIssues =
            issues.sortedWith(
                compareBy<
                        NutritionMatcherTrainingDatasetValidationIssue
                        >(
                    { it.code.name },
                    { it.catalogKey.orEmpty() },
                    { it.exampleId.orEmpty() },
                    { it.message }
                )
            )

        val result =
            NutritionMatcherTrainingDatasetValidationResult(
                valid =
                    sortedIssues.isEmpty(),
                datasetVersion =
                    dataset.version,
                datasetType =
                    dataset.datasetType,
                exampleCount =
                    dataset.examples.size,
                positiveCount =
                    dataset.examples.count {
                        it.label ==
                                NutritionMatcherTrainingLabel.POSITIVE
                    },
                negativeCount =
                    dataset.examples.count {
                        it.label ==
                                NutritionMatcherTrainingLabel.NEGATIVE
                    },
                issueCount =
                    sortedIssues.size,
                issues =
                    sortedIssues,
                validatedFile =
                    datasetFile.absolutePath
            )

        printResult(
            result = result,
            output = output
        )

        return result
    }

    fun validateOrThrow(
        datasetFile: File,
        output: PrintStream = System.out
    ): NutritionMatcherTrainingDatasetValidationResult {

        val result =
            validate(
                datasetFile = datasetFile,
                output = output
            )

        check(result.valid) {
            buildString {

                append(
                    "Nutrition matcher training dataset " +
                            "validation failed with "
                )

                append(
                    result.issueCount
                )

                append(
                    " issue(s)."
                )

                result.issues.forEach { issue ->

                    appendLine()
                    append(
                        issue.code
                    )
                    append(
                        ": "
                    )
                    append(
                        issue.message
                    )
                }
            }
        }

        return result
    }

    private fun readDataset(
        datasetFile: File
    ): NutritionMatcherTrainingDataset {

        return runCatching {

            Gson().fromJson(
                datasetFile.readText(),
                NutritionMatcherTrainingDataset::class.java
            )

        }.getOrElse { throwable ->

            throw IllegalArgumentException(
                "Could not parse nutrition matcher training " +
                        "dataset: " +
                        datasetFile.absolutePath,
                throwable
            )
        }
    }

    private fun validateDatasetMetadata(
        dataset: NutritionMatcherTrainingDataset,
        issues:
        MutableList<
                NutritionMatcherTrainingDatasetValidationIssue
                >
    ) {
        if (dataset.version != SUPPORTED_VERSION) {

            issues +=
                issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .UNSUPPORTED_VERSION,
                    message =
                        "Expected dataset version " +
                                "$SUPPORTED_VERSION but found " +
                                "${dataset.version}."
                )
        }

        if (dataset.datasetType != EXPECTED_DATASET_TYPE) {

            issues +=
                issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .INVALID_DATASET_TYPE,
                    message =
                        "Expected datasetType " +
                                "'$EXPECTED_DATASET_TYPE' but found " +
                                "'${dataset.datasetType}'."
                )
        }
    }

    private fun validateSummary(
        dataset: NutritionMatcherTrainingDataset,
        issues:
        MutableList<
                NutritionMatcherTrainingDatasetValidationIssue
                >
    ) {


        val examples =
            dataset.examples

        val actualAcceptedOriginalCount =
            examples.count {
                it.role ==
                        NutritionMatcherTrainingExampleRole
                            .ACCEPTED_ORIGINAL_MATCH
            }

        if (
            dataset.summary.acceptedOriginalMatchCount !=
            actualAcceptedOriginalCount
        ) {
            issues +=
                issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .SUMMARY_ACCEPTED_ORIGINAL_COUNT_MISMATCH,
                    message =
                        "Summary acceptedOriginalMatchCount=" +
                                "${dataset.summary.acceptedOriginalMatchCount}, " +
                                "actual=$actualAcceptedOriginalCount."
                )
        }

        val actualCatalogKeyCount =
            examples
                .map {
                    it.catalogKey
                }
                .distinct()
                .size

        val actualPositiveCount =
            examples.count {
                it.label ==
                        NutritionMatcherTrainingLabel.POSITIVE
            }

        val actualNegativeCount =
            examples.count {
                it.label ==
                        NutritionMatcherTrainingLabel.NEGATIVE
            }

        val actualAcceptedSelectedCount =
            examples.count {
                it.role ==
                        NutritionMatcherTrainingExampleRole
                            .ACCEPTED_SELECTED
            }

        val actualRejectedSelectedCount =
            examples.count {
                it.role ==
                        NutritionMatcherTrainingExampleRole
                            .REJECTED_SELECTED
            }

        val actualNoMatchCount =
            examples.count {
                it.role ==
                        NutritionMatcherTrainingExampleRole
                            .REJECTED_NO_MATCH_CANDIDATE
            }

        val actualAlternativeCount =
            examples.count {
                it.role ==
                        NutritionMatcherTrainingExampleRole
                            .NON_SELECTED_ALTERNATIVE
            }

        if (
            dataset.summary.exampleCount !=
            examples.size
        ) {
            issues +=
                issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .SUMMARY_EXAMPLE_COUNT_MISMATCH,
                    message =
                        "Summary exampleCount=" +
                                "${dataset.summary.exampleCount}, " +
                                "actual=${examples.size}."
                )
        }

        if (
            dataset.summary.positiveCount !=
            actualPositiveCount
        ) {
            issues +=
                issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .SUMMARY_POSITIVE_COUNT_MISMATCH,
                    message =
                        "Summary positiveCount=" +
                                "${dataset.summary.positiveCount}, " +
                                "actual=$actualPositiveCount."
                )
        }

        if (
            dataset.summary.negativeCount !=
            actualNegativeCount
        ) {
            issues +=
                issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .SUMMARY_NEGATIVE_COUNT_MISMATCH,
                    message =
                        "Summary negativeCount=" +
                                "${dataset.summary.negativeCount}, " +
                                "actual=$actualNegativeCount."
                )
        }

        if (
            dataset.summary.sourceCatalogKeyCount !=
            actualCatalogKeyCount
        ) {
            issues +=
                issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .SUMMARY_CATALOG_KEY_COUNT_MISMATCH,
                    message =
                        "Summary sourceCatalogKeyCount=" +
                                "${dataset.summary.sourceCatalogKeyCount}, " +
                                "actual=$actualCatalogKeyCount."
                )
        }

        if (
            dataset.summary.acceptedSelectedCount !=
            actualAcceptedSelectedCount
        ) {
            issues +=
                issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .SUMMARY_ACCEPTED_SELECTED_COUNT_MISMATCH,
                    message =
                        "Summary acceptedSelectedCount=" +
                                "${dataset.summary.acceptedSelectedCount}, " +
                                "actual=$actualAcceptedSelectedCount."
                )
        }

        if (
            dataset.summary.rejectedSelectedCount !=
            actualRejectedSelectedCount
        ) {
            issues +=
                issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .SUMMARY_REJECTED_SELECTED_COUNT_MISMATCH,
                    message =
                        "Summary rejectedSelectedCount=" +
                                "${dataset.summary.rejectedSelectedCount}, " +
                                "actual=$actualRejectedSelectedCount."
                )
        }

        if (
            dataset.summary.rejectedNoMatchCandidateCount !=
            actualNoMatchCount
        ) {
            issues +=
                issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .SUMMARY_NO_MATCH_COUNT_MISMATCH,
                    message =
                        "Summary rejectedNoMatchCandidateCount=" +
                                "${dataset.summary.rejectedNoMatchCandidateCount}, " +
                                "actual=$actualNoMatchCount."
                )
        }

        if (
            dataset.summary.nonSelectedAlternativeCount !=
            actualAlternativeCount
        ) {
            issues +=
                issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .SUMMARY_ALTERNATIVE_COUNT_MISMATCH,
                    message =
                        "Summary nonSelectedAlternativeCount=" +
                                "${dataset.summary.nonSelectedAlternativeCount}, " +
                                "actual=$actualAlternativeCount."
                )
        }
    }

    private fun validateUniqueness(
        examples: List<NutritionMatcherTrainingExample>,
        issues:
        MutableList<
                NutritionMatcherTrainingDatasetValidationIssue
                >
    ) {
        examples
            .groupBy {
                it.id
            }
            .filterValues {
                it.size > 1
            }
            .forEach { (id, duplicateExamples) ->

                issues +=
                    issue(
                        code =
                            NutritionMatcherTrainingDatasetValidationIssueCode
                                .DUPLICATE_EXAMPLE_ID,
                        exampleId =
                            id,
                        catalogKey =
                            duplicateExamples
                                .firstOrNull()
                                ?.catalogKey,
                        message =
                            "Example ID '$id' occurs " +
                                    "${duplicateExamples.size} times."
                    )
            }

        examples
            .groupBy {
                CandidateIdentity(
                    catalogKey =
                        it.catalogKey,
                    serverKey =
                        it.serverKey
                )
            }
            .filterValues {
                it.size > 1
            }
            .forEach { (identity, duplicateExamples) ->

                issues +=
                    issue(
                        code =
                            NutritionMatcherTrainingDatasetValidationIssueCode
                                .DUPLICATE_CANDIDATE_PAIR,
                        exampleId =
                            duplicateExamples
                                .firstOrNull()
                                ?.id,
                        catalogKey =
                            identity.catalogKey,
                        message =
                            "Candidate pair '${identity.catalogKey}' -> " +
                                    "'${identity.serverKey}' occurs " +
                                    "${duplicateExamples.size} times."
                    )
            }
    }

    private fun validateDeterministicOrder(
        examples: List<NutritionMatcherTrainingExample>,
        issues:
        MutableList<
                NutritionMatcherTrainingDatasetValidationIssue
                >
    ) {
        val expected =
            examples.sortedWith(
                compareBy<NutritionMatcherTrainingExample>(
                    { it.catalogKey },
                    { it.candidateRank },
                    { it.serverKey },
                    { it.id }
                )
            )

        if (examples != expected) {

            issues +=
                issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .NON_DETERMINISTIC_ORDER,
                    message =
                        "Examples are not sorted by catalogKey, " +
                                "candidateRank, serverKey and id."
                )
        }
    }

    private fun validateExample(
        example: NutritionMatcherTrainingExample,
        issues:
        MutableList<
                NutritionMatcherTrainingDatasetValidationIssue
                >
    ) {
        val context =
            ExampleContext(
                id =
                    example.id,
                catalogKey =
                    example.catalogKey
            )

        validateStableId(
            example = example,
            context = context,
            issues = issues
        )

        validateKeys(
            example = example,
            context = context,
            issues = issues
        )

        validateCandidateValues(
            example = example,
            context = context,
            issues = issues
        )

        validateCollections(
            example = example,
            context = context,
            issues = issues
        )

        validateLabelAndRole(
            example = example,
            context = context,
            issues = issues
        )

        validateProvenance(
            example = example,
            context = context,
            issues = issues
        )
    }

    private fun validateStableId(
        example: NutritionMatcherTrainingExample,
        context: ExampleContext,
        issues:
        MutableList<
                NutritionMatcherTrainingDatasetValidationIssue
                >
    ) {
        val expectedId =
            createStableId(
                catalogKey =
                    example.catalogKey,
                serverKey =
                    example.serverKey,
                label =
                    example.label,
                role =
                    example.role
            )

        if (example.id != expectedId) {

            issues +=
                context.issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .INVALID_STABLE_ID,
                    message =
                        "Expected stable ID '$expectedId' but found " +
                                "'${example.id}'."
                )
        }
    }

    private fun validateKeys(
        example: NutritionMatcherTrainingExample,
        context: ExampleContext,
        issues:
        MutableList<
                NutritionMatcherTrainingDatasetValidationIssue
                >
    ) {
        if (
            example.catalogKey.isBlank() ||
            example.catalogKey !=
            normalizeKey(example.catalogKey)
        ) {
            issues +=
                context.issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .INVALID_CATALOG_KEY,
                    message =
                        "Catalog key must be non-blank and normalized: " +
                                "'${example.catalogKey}'."
                )
        }

        if (
            example.serverKey.isBlank() ||
            example.serverKey !=
            normalizeKey(example.serverKey)
        ) {
            issues +=
                context.issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .INVALID_SERVER_KEY,
                    message =
                        "Server key must be non-blank and normalized: " +
                                "'${example.serverKey}'."
                )
        }

        if (
            example.serverArtifact !=
            NUTRITION_ARTIFACT
        ) {
            issues +=
                context.issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .INVALID_SERVER_ARTIFACT,
                    message =
                        "Expected serverArtifact " +
                                "'$NUTRITION_ARTIFACT' but found " +
                                "'${example.serverArtifact}'."
                )
        }
    }

    private fun validateCandidateValues(
        example: NutritionMatcherTrainingExample,
        context: ExampleContext,
        issues:
        MutableList<
                NutritionMatcherTrainingDatasetValidationIssue
                >
    ) {

        if (
            !example.diagnosticScoreAvailable &&
            example.diagnosticScore != 0.0
        ) {
            issues +=
                context.issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .INVALID_DIAGNOSTIC_SCORE_AVAILABILITY,
                    message =
                        "Unavailable diagnosticScore must be 0.0."
                )
        }

        if (example.candidateCount <= 0) {

            issues +=
                context.issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .INVALID_CANDIDATE_COUNT,
                    message =
                        "candidateCount must be greater than zero."
                )
        }

        if (
            example.candidateRank <= 0 ||
            example.candidateRank >
            example.candidateCount
        ) {
            issues +=
                context.issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .INVALID_CANDIDATE_RANK,
                    message =
                        "candidateRank=${example.candidateRank} is not " +
                                "within 1..${example.candidateCount}."
                )
        }

        if (
            !example.diagnosticScore.isFinite() ||
            example.diagnosticScore < 0.0
        ) {
            issues +=
                context.issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .INVALID_DIAGNOSTIC_SCORE,
                    message =
                        "diagnosticScore must be finite and " +
                                "non-negative."
                )
        }

        if (
            !example.matcherConfidence.isFinite() ||
            example.matcherConfidence !in 0.0..1.0
        ) {
            issues +=
                context.issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .INVALID_MATCHER_CONFIDENCE,
                    message =
                        "matcherConfidence must be between " +
                                "0.0 and 1.0."
                )
        }

        if (
            !example.trainingWeight.isFinite() ||
            example.trainingWeight <= 0.0 ||
            example.trainingWeight > 1.0
        ) {
            issues +=
                context.issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .INVALID_TRAINING_WEIGHT,
                    message =
                        "trainingWeight must be in (0.0, 1.0]."
                )
        }
    }

    private fun validateCollections(
        example: NutritionMatcherTrainingExample,
        context: ExampleContext,
        issues:
        MutableList<
                NutritionMatcherTrainingDatasetValidationIssue
                >
    ) {
        if (
            example.sharedTokens.size !=
            example.sharedTokens.distinct().size
        ) {
            issues +=
                context.issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .DUPLICATE_SHARED_TOKEN,
                    message =
                        "sharedTokens contains duplicates."
                )
        }

        if (
            example.sharedTokens !=
            example.sharedTokens.sorted()
        ) {
            issues +=
                context.issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .UNSORTED_SHARED_TOKENS,
                    message =
                        "sharedTokens must be sorted."
                )
        }

        if (
            example.representativeReasons.size !=
            example.representativeReasons
                .distinct()
                .size
        ) {
            issues +=
                context.issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .DUPLICATE_REPRESENTATIVE_REASON,
                    message =
                        "representativeReasons contains duplicates."
                )
        }

        if (
            example.representativeReasons !=
            example.representativeReasons.sorted()
        ) {
            issues +=
                context.issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .UNSORTED_REPRESENTATIVE_REASONS,
                    message =
                        "representativeReasons must be sorted."
                )
        }
    }

    private fun validateLabelAndRole(
        example: NutritionMatcherTrainingExample,
        context: ExampleContext,
        issues:
        MutableList<
                NutritionMatcherTrainingDatasetValidationIssue
                >
    ) {
        when (example.role) {

            NutritionMatcherTrainingExampleRole
                .ACCEPTED_ORIGINAL_MATCH -> {

                if (
                    example.label !=
                    NutritionMatcherTrainingLabel.POSITIVE
                ) {
                    issues +=
                        context.issue(
                            code =
                                NutritionMatcherTrainingDatasetValidationIssueCode
                                    .INVALID_POSITIVE_ROLE,
                            message =
                                "ACCEPTED_ORIGINAL_MATCH must have " +
                                        "label POSITIVE."
                        )
                }

                if (!example.selected) {
                    issues +=
                        context.issue(
                            code =
                                NutritionMatcherTrainingDatasetValidationIssueCode
                                    .INVALID_SELECTED_STATE,
                            message =
                                "ACCEPTED_ORIGINAL_MATCH must be selected."
                        )
                }

                if (
                    example.originalValidationStatus !=
                    CatalogKnowledgeMappingValidationStatus.ACCEPTED.name
                ) {
                    issues +=
                        context.issue(
                            code =
                                NutritionMatcherTrainingDatasetValidationIssueCode
                                    .INVALID_POSITIVE_ROLE,
                            message =
                                "ACCEPTED_ORIGINAL_MATCH must originate " +
                                        "from validationStatus ACCEPTED."
                        )
                }

                if (
                    example.representativeDecisionType != null ||
                    example.representativeReasons.isNotEmpty()
                ) {
                    issues +=
                        context.issue(
                            code =
                                NutritionMatcherTrainingDatasetValidationIssueCode
                                    .INVALID_REPRESENTATIVE_DECISION,
                            message =
                                "ACCEPTED_ORIGINAL_MATCH must not contain " +
                                        "representative validation data."
                        )
                }
            }

            NutritionMatcherTrainingExampleRole
                .ACCEPTED_SELECTED -> {

                if (
                    example.label !=
                    NutritionMatcherTrainingLabel.POSITIVE
                ) {
                    issues +=
                        context.issue(
                            code =
                                NutritionMatcherTrainingDatasetValidationIssueCode
                                    .INVALID_POSITIVE_ROLE,
                            message =
                                "ACCEPTED_SELECTED must have label " +
                                        "POSITIVE."
                        )
                }

                if (!example.selected) {
                    issues +=
                        context.issue(
                            code =
                                NutritionMatcherTrainingDatasetValidationIssueCode
                                    .INVALID_SELECTED_STATE,
                            message =
                                "ACCEPTED_SELECTED must be selected."
                        )
                }

                if (
                    example.representativeDecisionType !=
                    IDENTICAL &&
                    example.representativeDecisionType !=
                    REPRESENTATIVE
                ) {
                    issues +=
                        context.issue(
                            code =
                                NutritionMatcherTrainingDatasetValidationIssueCode
                                    .INVALID_REPRESENTATIVE_DECISION,
                            message =
                                "ACCEPTED_SELECTED must have " +
                                        "IDENTICAL or REPRESENTATIVE " +
                                        "decision."
                        )
                }
            }

            NutritionMatcherTrainingExampleRole
                .REJECTED_SELECTED -> {

                if (
                    example.label !=
                    NutritionMatcherTrainingLabel.NEGATIVE
                ) {
                    issues +=
                        context.issue(
                            code =
                                NutritionMatcherTrainingDatasetValidationIssueCode
                                    .INVALID_NEGATIVE_ROLE,
                            message =
                                "REJECTED_SELECTED must have label " +
                                        "NEGATIVE."
                        )
                }

                if (!example.selected) {
                    issues +=
                        context.issue(
                            code =
                                NutritionMatcherTrainingDatasetValidationIssueCode
                                    .INVALID_SELECTED_STATE,
                            message =
                                "REJECTED_SELECTED must be selected."
                        )
                }

                if (
                    example.representativeDecisionType !=
                    INCOMPATIBLE
                ) {
                    issues +=
                        context.issue(
                            code =
                                NutritionMatcherTrainingDatasetValidationIssueCode
                                    .INVALID_REPRESENTATIVE_DECISION,
                            message =
                                "REJECTED_SELECTED must have " +
                                        "INCOMPATIBLE decision."
                        )
                }
            }

            NutritionMatcherTrainingExampleRole
                .REJECTED_NO_MATCH_CANDIDATE -> {

                if (
                    example.label !=
                    NutritionMatcherTrainingLabel.NEGATIVE
                ) {
                    issues +=
                        context.issue(
                            code =
                                NutritionMatcherTrainingDatasetValidationIssueCode
                                    .INVALID_NEGATIVE_ROLE,
                            message =
                                "REJECTED_NO_MATCH_CANDIDATE must " +
                                        "have label NEGATIVE."
                        )
                }

                if (example.selected) {
                    issues +=
                        context.issue(
                            code =
                                NutritionMatcherTrainingDatasetValidationIssueCode
                                    .INVALID_SELECTED_STATE,
                            message =
                                "REJECTED_NO_MATCH_CANDIDATE must " +
                                        "not be selected."
                        )
                }

                if (
                    example.originalValidationStatus !=
                    REJECTED_NO_MATCH
                ) {
                    issues +=
                        context.issue(
                            code =
                                NutritionMatcherTrainingDatasetValidationIssueCode
                                    .INVALID_NO_MATCH_STATUS,
                            message =
                                "REJECTED_NO_MATCH_CANDIDATE must " +
                                        "originate from " +
                                        "REJECTED_NO_MATCH."
                        )
                }

                if (
                    example.representativeDecisionType !=
                    null
                ) {
                    issues +=
                        context.issue(
                            code =
                                NutritionMatcherTrainingDatasetValidationIssueCode
                                    .INVALID_REPRESENTATIVE_DECISION,
                            message =
                                "NO_MATCH candidate must not contain " +
                                        "a representative decision."
                        )
                }
            }

            NutritionMatcherTrainingExampleRole
                .NON_SELECTED_ALTERNATIVE -> {

                if (
                    example.label !=
                    NutritionMatcherTrainingLabel.NEGATIVE
                ) {
                    issues +=
                        context.issue(
                            code =
                                NutritionMatcherTrainingDatasetValidationIssueCode
                                    .INVALID_NEGATIVE_ROLE,
                            message =
                                "NON_SELECTED_ALTERNATIVE must have " +
                                        "label NEGATIVE."
                        )
                }

                if (example.selected) {
                    issues +=
                        context.issue(
                            code =
                                NutritionMatcherTrainingDatasetValidationIssueCode
                                    .INVALID_SELECTED_STATE,
                            message =
                                "NON_SELECTED_ALTERNATIVE must not " +
                                        "be selected."
                        )
                }
            }
        }
    }

    private fun validateProvenance(
        example: NutritionMatcherTrainingExample,
        context: ExampleContext,
        issues:
        MutableList<
                NutritionMatcherTrainingDatasetValidationIssue
                >
    ) {
        val provenance =
            example.provenance

        val valid =
            provenance.sourceType.isNotBlank() &&
                    provenance.candidateQualityFile.isNotBlank() &&
                    provenance.diagnosticsFile.isNotBlank() &&
                    provenance.representativeValidationFile
                        .isNotBlank() &&
                    provenance.sourceVersion ==
                    SUPPORTED_VERSION &&
                    provenance.matcher.isNotBlank() &&
                    provenance.validator.isNotBlank()

        if (!valid) {

            issues +=
                context.issue(
                    code =
                        NutritionMatcherTrainingDatasetValidationIssueCode
                            .INVALID_PROVENANCE,
                    message =
                        "Training example has incomplete or " +
                                "unsupported provenance."
                )
        }
    }

    private fun createStableId(
        catalogKey: String,
        serverKey: String,
        label: NutritionMatcherTrainingLabel,
        role: NutritionMatcherTrainingExampleRole
    ): String {

        val canonicalValue =
            listOf(
                DATASET_NAMESPACE,
                catalogKey,
                NUTRITION_ARTIFACT,
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

    private fun printResult(
        result:
        NutritionMatcherTrainingDatasetValidationResult,
        output: PrintStream
    ) {
        output.println()
        output.println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
        output.println(
            "NUTRITION MATCHER DATASET VALIDATION"
        )
        output.println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
        output.println(
            "Valid                     : " +
                    result.valid
        )
        output.println(
            "Examples                  : " +
                    result.exampleCount
        )
        output.println(
            "Positive                  : " +
                    result.positiveCount
        )
        output.println(
            "Negative                  : " +
                    result.negativeCount
        )
        output.println(
            "Issues                    : " +
                    result.issueCount
        )

        if (result.issues.isNotEmpty()) {

            output.println()

            result.issues.forEach { issue ->

                output.println(
                    "${issue.code}: ${issue.message}"
                )
            }
        }

        output.println()
        output.println(
            "Dataset                   : " +
                    result.validatedFile
        )
        output.println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
    }

    private fun issue(
        code:
        NutritionMatcherTrainingDatasetValidationIssueCode,
        exampleId: String? = null,
        catalogKey: String? = null,
        message: String
    ): NutritionMatcherTrainingDatasetValidationIssue {

        return NutritionMatcherTrainingDatasetValidationIssue(
            code = code,
            exampleId = exampleId,
            catalogKey = catalogKey,
            message = message
        )
    }

    private fun ExampleContext.issue(
        code:
        NutritionMatcherTrainingDatasetValidationIssueCode,
        message: String
    ): NutritionMatcherTrainingDatasetValidationIssue {

        return issue(
            code = code,
            exampleId = id,
            catalogKey = catalogKey,
            message = message
        )
    }

    private fun normalizeKey(
        value: String
    ): String {

        return value
            .trim()
            .lowercase()
            .replace("-", " ")
            .replace("_", " ")
            .replace(
                WHITESPACE_REGEX,
                " "
            )
            .trim()
    }

    private data class CandidateIdentity(
        val catalogKey: String,
        val serverKey: String
    )

    private data class ExampleContext(
        val id: String,
        val catalogKey: String
    )

    private companion object {

        const val ACCEPTED =
            "ACCEPTED"

        const val SUPPORTED_VERSION =
            1

        const val EXPECTED_DATASET_TYPE =
            "NUTRITION_CATALOG_SERVER_MATCHER"

        const val NUTRITION_ARTIFACT =
            "nutrition.json"

        const val DATASET_NAMESPACE =
            "nutrition-catalog-server-matcher-v1"

        const val IDENTICAL =
            "IDENTICAL"

        const val REPRESENTATIVE =
            "REPRESENTATIVE"

        const val INCOMPATIBLE =
            "INCOMPATIBLE"

        const val REJECTED_NO_MATCH =
            "REJECTED_NO_MATCH"

        val WHITESPACE_REGEX =
            Regex("\\s+")
    }
}