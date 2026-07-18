package de.shopme.tools.knowledge.mapping.catalog.training

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class NutritionMatcherTrainingDatasetExporter {

    fun run(
        candidateQualityFile: File,
        diagnosticsFile: File,
        representativeValidationFile: File,
        mappingFile: File,
        outputFile: File,
        output: PrintStream = System.out
    ): ExportNutritionMatcherTrainingDatasetResult {

        require(mappingFile.isFile) {
            "Catalog-server mapping file does not exist: " +
                    mappingFile.absolutePath
        }

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

        val diagnostics =
            readDiagnostics(
                file = diagnosticsFile
            )

        val representativeValidations =
            readRepresentativeValidations(
                file = representativeValidationFile
            )

        val persistedMappings =
            readPersistedMappings(
                file = mappingFile
            )

        val candidateQuality =
            readCandidateQuality(
                file = candidateQualityFile
            )

        val rejectedExamples =
            candidateQuality
                .flatMap { entry ->

                    val diagnostic =
                        diagnostics[entry.catalogKey]
                            ?: error(
                                "Missing nutrition match diagnostic " +
                                        "for candidate quality entry: " +
                                        entry.catalogKey
                            )

                    val representativeValidation =
                        representativeValidations[
                            entry.catalogKey
                        ]

                    entry.candidates.map { candidate ->

                        toTrainingExample(
                            entry = entry,
                            candidate = candidate,
                            diagnostic = diagnostic,
                            representativeValidation =
                                representativeValidation,
                            candidateQualityFile =
                                candidateQualityFile,
                            diagnosticsFile =
                                diagnosticsFile,
                            representativeValidationFile =
                                representativeValidationFile
                        )
                    }
                }

        val rejectedCandidatePairs =
            rejectedExamples
                .map {
                    CandidateIdentity(
                        catalogKey = it.catalogKey,
                        serverKey = it.serverKey,
                    )
                }
                .toHashSet()

        val acceptedOriginalExamples =
            diagnostics
                .values
                .asSequence()
                .filter {
                    it.validationStatus == ACCEPTED
                }
                .filter {
                    it.mappingWritten
                }
                .filter {
                    it.decisionType == MATCH
                }
                .map { diagnostic ->

                    toAcceptedOriginalTrainingExample(
                        diagnostic = diagnostic,
                        persistedMappings = persistedMappings,
                        candidateQualityFile = candidateQualityFile,
                        diagnosticsFile = diagnosticsFile,
                        representativeValidationFile = representativeValidationFile,
                    )
                }
                .filterNot { example ->

                    CandidateIdentity(
                        catalogKey = example.catalogKey,
                        serverKey = example.serverKey,
                    ) in rejectedCandidatePairs
                }
                .toList()

        val examples =
            (
                    rejectedExamples +
                            acceptedOriginalExamples
                    )
                .sortedWith(
                    compareBy<NutritionMatcherTrainingExample>(
                        { it.catalogKey },
                        { it.candidateRank },
                        { it.serverKey },
                        { it.id }
                    )
                )



        validateExamples(
            examples = examples
        )

        val dataset =
            NutritionMatcherTrainingDataset(
                summary =
                    createSummary(
                        candidateQuality =
                            candidateQuality,
                        examples =
                            examples
                    ),
                examples =
                    examples
            )

        writeDataset(
            dataset = dataset,
            outputFile = outputFile
        )

        printDataset(
            dataset = dataset,
            outputFile = outputFile,
            output = output
        )

        return ExportNutritionMatcherTrainingDatasetResult(
            dataset = dataset,
            outputFile = outputFile.absolutePath
        )
    }

    private fun toTrainingExample(
        entry: CandidateQualityEntry,
        candidate: CandidateQualityCandidate,
        diagnostic: MatchDiagnostic,
        representativeValidation:
        RepresentativeValidation?,
        candidateQualityFile: File,
        diagnosticsFile: File,
        representativeValidationFile: File
    ): NutritionMatcherTrainingExample {

        val classification =
            classify(
                entry = entry,
                candidate = candidate,
                diagnostic = diagnostic,
                representativeValidation =
                    representativeValidation
            )

        return NutritionMatcherTrainingExample(
            id =
                createStableId(
                    catalogKey =
                        entry.catalogKey,
                    serverKey =
                        candidate.serverKey,
                    label =
                        classification.label,
                    role =
                        classification.role
                ),
            catalogKey =
                entry.catalogKey,
            serverArtifact =
                NUTRITION_ARTIFACT,
            serverKey =
                candidate.serverKey,
            label =
                classification.label,
            role =
                classification.role,
            selected =
                candidate.selected,
            candidateRank =
                candidate.rank,
            candidateCount =
                entry.candidates.size,
            diagnosticScore =
                candidate.diagnosticScore,
            diagnosticScoreAvailable =
                true,
            sharedTokens =
                candidate.sharedTokens
                    .distinct()
                    .sorted(),
            matcherConfidence =
                diagnostic.confidence,
            originalDecisionType =
                diagnostic.decisionType,
            originalDecisionReason =
                diagnostic.decisionReason,
            originalValidationStatus =
                diagnostic.validationStatus,
            originalValidationReason =
                diagnostic.validationReason,
            representativeDecisionType =
                representativeValidation
                    ?.decisionType,
            representativeReasons =
                representativeValidation
                    ?.reasons
                    .orEmpty()
                    .distinct()
                    .sorted(),
            trainingWeight =
                classification.trainingWeight,
            provenance =
                NutritionMatcherTrainingProvenance(
                    sourceType =
                        SOURCE_TYPE,
                    candidateQualityFile =
                        candidateQualityFile.name,
                    diagnosticsFile =
                        diagnosticsFile.name,
                    representativeValidationFile =
                        representativeValidationFile.name,
                    sourceVersion =
                        SOURCE_VERSION,
                    matcher =
                        MATCHER,
                    validator =
                        VALIDATOR
                )
        )
    }

    private fun classify(
        entry: CandidateQualityEntry,
        candidate: CandidateQualityCandidate,
        diagnostic: MatchDiagnostic,
        representativeValidation:
        RepresentativeValidation?
    ): Classification {

        if (candidate.selected) {

            val candidateMatchesDiagnosticSelection =
                diagnostic.selectedServerKey ==
                        candidate.serverKey

            val candidateMatchesRepresentativeSelection =
                representativeValidation?.selectedServerKey ==
                        candidate.serverKey

            require(
                candidateMatchesDiagnosticSelection ||
                        candidateMatchesRepresentativeSelection,
            ) {
                "Selected candidate differs from both diagnostic and " +
                        "accepted representative selection for " +
                        "'${entry.catalogKey}': " +
                        "candidate='${candidate.serverKey}', " +
                        "diagnostic='${diagnostic.selectedServerKey}', " +
                        "representative='" +
                        "${representativeValidation?.selectedServerKey}', " +
                        "representativeAccepted=" +
                        "${representativeValidation?.accepted}"
            }


            requireNotNull(representativeValidation) {
                "Selected low-confidence candidate has no " +
                        "representative validation: " +
                        "${entry.catalogKey} -> " +
                        candidate.serverKey
            }

            require(
                representativeValidation.selectedServerKey ==
                        candidate.serverKey
            ) {
                "Representative validation server key differs " +
                        "from selected candidate for " +
                        "'${entry.catalogKey}'."
            }

            return if (representativeValidation.accepted) {

                require(
                    representativeValidation.decisionType ==
                            IDENTICAL ||
                            representativeValidation.decisionType ==
                            REPRESENTATIVE
                ) {
                    "Accepted representative validation must be " +
                            "IDENTICAL or REPRESENTATIVE: " +
                            "${entry.catalogKey} -> " +
                            candidate.serverKey
                }

                Classification(
                    label =
                        NutritionMatcherTrainingLabel.POSITIVE,
                    role =
                        NutritionMatcherTrainingExampleRole
                            .ACCEPTED_SELECTED,
                    trainingWeight =
                        POSITIVE_WEIGHT
                )

            } else {

                require(
                    representativeValidation.decisionType ==
                            INCOMPATIBLE
                ) {
                    "Rejected representative validation must be " +
                            "INCOMPATIBLE: " +
                            "${entry.catalogKey} -> " +
                            candidate.serverKey
                }

                Classification(
                    label =
                        NutritionMatcherTrainingLabel.NEGATIVE,
                    role =
                        NutritionMatcherTrainingExampleRole
                            .REJECTED_SELECTED,
                    trainingWeight =
                        REJECTED_SELECTED_WEIGHT
                )
            }
        }

        if (
            diagnostic.validationStatus ==
            REJECTED_NO_MATCH
        ) {
            return Classification(
                label =
                    NutritionMatcherTrainingLabel.NEGATIVE,
                role =
                    NutritionMatcherTrainingExampleRole
                        .REJECTED_NO_MATCH_CANDIDATE,
                trainingWeight =
                    NO_MATCH_NEGATIVE_WEIGHT
            )
        }

        return Classification(
            label =
                NutritionMatcherTrainingLabel.NEGATIVE,
            role =
                NutritionMatcherTrainingExampleRole
                    .NON_SELECTED_ALTERNATIVE,
            trainingWeight =
                ALTERNATIVE_NEGATIVE_WEIGHT
        )
    }

    private fun createSummary(
        candidateQuality:
        List<CandidateQualityEntry>,
        examples:
        List<NutritionMatcherTrainingExample>
    ): NutritionMatcherTrainingDatasetSummary {

        return NutritionMatcherTrainingDatasetSummary(
            sourceCatalogKeyCount =
                examples
                    .map {
                        it.catalogKey
                    }
                    .distinct()
                    .size,
            exampleCount =
                examples.size,
            positiveCount =
                examples.count {
                    it.label ==
                            NutritionMatcherTrainingLabel.POSITIVE
                },
            negativeCount =
                examples.count {
                    it.label ==
                            NutritionMatcherTrainingLabel.NEGATIVE
                },
            acceptedOriginalMatchCount =
                examples.count {
                    it.role ==
                            NutritionMatcherTrainingExampleRole
                                .ACCEPTED_ORIGINAL_MATCH
                },
            acceptedSelectedCount =
                examples.count {
                    it.role ==
                            NutritionMatcherTrainingExampleRole
                                .ACCEPTED_SELECTED
                },
            rejectedSelectedCount =
                examples.count {
                    it.role ==
                            NutritionMatcherTrainingExampleRole
                                .REJECTED_SELECTED
                },
            rejectedNoMatchCandidateCount =
                examples.count {
                    it.role ==
                            NutritionMatcherTrainingExampleRole
                                .REJECTED_NO_MATCH_CANDIDATE
                },
            nonSelectedAlternativeCount =
                examples.count {
                    it.role ==
                            NutritionMatcherTrainingExampleRole
                                .NON_SELECTED_ALTERNATIVE
                }
        )
    }

    private fun toAcceptedOriginalTrainingExample(
        diagnostic: MatchDiagnostic,
        persistedMappings: Map<String, PersistedMapping>,
        candidateQualityFile: File,
        diagnosticsFile: File,
        representativeValidationFile: File
    ): NutritionMatcherTrainingExample {

        val selectedServerKey =
            requireNotNull(
                diagnostic.selectedServerKey
            ) {
                "Accepted nutrition diagnostic has no " +
                        "selectedServerKey: " +
                        diagnostic.catalogKey
            }

        val persistedMapping =
            persistedMappings[diagnostic.catalogKey]
                ?: error(
                    "Accepted nutrition diagnostic has no persisted " +
                            "catalog-server mapping: " +
                            diagnostic.catalogKey
                )

        require(
            persistedMapping.serverArtifact ==
                    NUTRITION_ARTIFACT
        ) {
            "Accepted mapping uses unexpected artifact: " +
                    "${diagnostic.catalogKey} -> " +
                    persistedMapping.serverArtifact
        }

        require(
            persistedMapping.serverKey ==
                    selectedServerKey
        ) {
            "Accepted diagnostic and persisted mapping differ: " +
                    "${diagnostic.catalogKey}, " +
                    "diagnostic='$selectedServerKey', " +
                    "mapping='${persistedMapping.serverKey}'"
        }

        val candidateRank =
            diagnostic.candidateServerKeys
                .indexOf(
                    selectedServerKey
                )
                .takeIf {
                    it >= 0
                }
                ?.plus(1)
                ?: error(
                    "Accepted selected candidate is not present in " +
                            "candidateServerKeys: " +
                            "${diagnostic.catalogKey} -> " +
                            selectedServerKey
                )

        require(
            diagnostic.candidateCount ==
                    diagnostic.candidateServerKeys.size
        ) {
            "Accepted diagnostic candidate count differs from " +
                    "candidateServerKeys size: " +
                    diagnostic.catalogKey
        }

        val sharedTokens =
            calculateSharedTokens(
                catalogKey =
                    diagnostic.catalogKey,
                serverKey =
                    selectedServerKey
            )

        return NutritionMatcherTrainingExample(
            id =
                createStableId(
                    catalogKey =
                        diagnostic.catalogKey,
                    serverKey =
                        selectedServerKey,
                    label =
                        NutritionMatcherTrainingLabel.POSITIVE,
                    role =
                        NutritionMatcherTrainingExampleRole
                            .ACCEPTED_ORIGINAL_MATCH
                ),
            catalogKey =
                diagnostic.catalogKey,
            serverArtifact =
                NUTRITION_ARTIFACT,
            serverKey =
                selectedServerKey,
            label =
                NutritionMatcherTrainingLabel.POSITIVE,
            role =
                NutritionMatcherTrainingExampleRole
                    .ACCEPTED_ORIGINAL_MATCH,
            selected =
                true,
            candidateRank =
                candidateRank,
            candidateCount =
                diagnostic.candidateCount,
            diagnosticScore =
                0.0,
            diagnosticScoreAvailable =
                false,
            sharedTokens =
                sharedTokens,
            matcherConfidence =
                diagnostic.confidence,
            originalDecisionType =
                diagnostic.decisionType,
            originalDecisionReason =
                diagnostic.decisionReason,
            originalValidationStatus =
                diagnostic.validationStatus,
            originalValidationReason =
                diagnostic.validationReason,
            representativeDecisionType =
                null,
            representativeReasons =
                emptyList(),
            trainingWeight =
                POSITIVE_WEIGHT,
            provenance =
                NutritionMatcherTrainingProvenance(
                    sourceType =
                        SOURCE_TYPE,
                    candidateQualityFile =
                        candidateQualityFile.name,
                    diagnosticsFile =
                        diagnosticsFile.name,
                    representativeValidationFile =
                        representativeValidationFile.name,
                    sourceVersion =
                        SOURCE_VERSION,
                    matcher =
                        MATCHER,
                    validator =
                        ORIGINAL_VALIDATOR
                )
        )
    }

    private fun validateExamples(
        examples:
        List<NutritionMatcherTrainingExample>
    ) {
        val duplicateIds =
            examples
                .groupingBy {
                    it.id
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicateIds.isEmpty()) {
            "Duplicate nutrition matcher training IDs: " +
                    duplicateIds
                        .sorted()
                        .joinToString()
        }

        val duplicatePairs =
            examples
                .groupingBy {
                    CandidateIdentity(
                        catalogKey =
                            it.catalogKey,
                        serverKey =
                            it.serverKey
                    )
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicatePairs.isEmpty()) {
            "Duplicate nutrition matcher training candidates: " +
                    duplicatePairs
                        .sortedWith(
                            compareBy<CandidateIdentity>(
                                { it.catalogKey },
                                { it.serverKey }
                            )
                        )
                        .joinToString {
                            "${it.catalogKey} -> ${it.serverKey}"
                        }
        }

        require(
            examples.all {
                it.candidateRank > 0
            }
        ) {
            "Every nutrition matcher training example must " +
                    "have a positive candidate rank."
        }

        require(
            examples.all {
                it.matcherConfidence in 0.0..1.0
            }
        ) {
            "Every nutrition matcher training example must " +
                    "have confidence between 0.0 and 1.0."
        }

        require(
            examples.all {
                it.trainingWeight > 0.0 &&
                        it.trainingWeight <= 1.0
            }
        ) {
            "Every nutrition matcher training example must " +
                    "have training weight in (0.0, 1.0]."
        }
    }

    private fun readCandidateQuality(
        file: File
    ): List<CandidateQualityEntry> {

        val root =
            parseRoot(
                file = file
            )

        val entries =
            root.requiredArray(
                key = "entries",
                source = file.absolutePath
            )

        return entries
            .map { element ->

                val json =
                    element.asJsonObject

                val catalogKey =
                    normalizeKey(
                        json.requiredString(
                            key = "catalogKey",
                            source =
                                "candidate quality entry"
                        )
                    )

                val candidates =
                    json.requiredArray(
                        key = "candidates",
                        source =
                            "candidate quality entry '$catalogKey'"
                    )
                        .mapIndexed { index, candidateElement ->

                            val candidate =
                                candidateElement.asJsonObject

                            CandidateQualityCandidate(
                                rank =
                                    index + 1,
                                serverKey =
                                    normalizeKey(
                                        candidate.requiredString(
                                            key = "serverKey",
                                            source =
                                                "candidate for " +
                                                        "'$catalogKey'"
                                        )
                                    ),
                                diagnosticScore =
                                    candidate.requiredDouble(
                                        key = "diagnosticScore",
                                        source =
                                            "candidate for " +
                                                    "'$catalogKey'"
                                    ),
                                sharedTokens =
                                    candidate.requiredStringArray(
                                        key = "sharedTokens",
                                        source =
                                            "candidate for " +
                                                    "'$catalogKey'"
                                    ),
                                selected =
                                    candidate.optionalBoolean(
                                        key = "selected"
                                    )
                                        ?: false
                            )
                        }

                require(candidates.isNotEmpty()) {
                    "Training dataset cannot export an empty " +
                            "candidate list for '$catalogKey'."
                }

                val selectedCandidateRank =
                    json.optionalInt(
                        key = "selectedCandidateRank"
                    )

                selectedCandidateRank?.let { rank ->

                    require(rank in 1..candidates.size) {
                        "Selected candidate rank is outside the " +
                                "candidate list for '$catalogKey': " +
                                rank
                    }

                    require(
                        candidates.count {
                            it.selected
                        } == 1
                    ) {
                        "Expected exactly one selected candidate " +
                                "for '$catalogKey'."
                    }

                    require(
                        candidates[rank - 1].selected
                    ) {
                        "Selected candidate rank and selected flag " +
                                "differ for '$catalogKey'."
                    }
                }

                CandidateQualityEntry(
                    catalogKey =
                        catalogKey,
                    candidates =
                        candidates
                )
            }
            .sortedBy {
                it.catalogKey
            }
    }

    private fun readDiagnostics(
        file: File
    ): Map<String, MatchDiagnostic> {

        val root =
            parseRoot(
                file = file
            )

        val diagnostics =
            root.requiredArray(
                key = "diagnostics",
                source = file.absolutePath
            )
                .map { element ->

                    val json =
                        element.asJsonObject

                    MatchDiagnostic(
                        candidateCount =
                            json.requiredInt(
                                key = "candidateCount",
                                source =
                                    "nutrition match diagnostic"
                            ),
                        candidateServerKeys =
                            json.requiredOrderedStringArray(
                                key = "candidateServerKeys",
                                source =
                                    "nutrition match diagnostic"
                            )
                                .map {
                                    normalizeKey(it)
                                },
                        mappingWritten =
                            json.requiredBoolean(
                                key = "mappingWritten",
                                source =
                                    "nutrition match diagnostic"
                            ),
                        catalogKey =
                            normalizeKey(
                                json.requiredString(
                                    key = "catalogKey",
                                    source =
                                        "nutrition match diagnostic"
                                )
                            ),
                        decisionType =
                            json.requiredString(
                                key = "decisionType",
                                source =
                                    "nutrition match diagnostic"
                            ),
                        selectedServerKey =
                            json.optionalString(
                                key = "selectedServerKey"
                            )
                                ?.let {
                                    normalizeKey(it)
                                },
                        confidence =
                            json.requiredDouble(
                                key = "confidence",
                                source =
                                    "nutrition match diagnostic"
                            ),
                        decisionReason =
                            json.optionalString(
                                key = "decisionReason"
                            ),
                        validationStatus =
                            json.requiredString(
                                key = "validationStatus",
                                source =
                                    "nutrition match diagnostic"
                            ),
                        validationReason =
                            json.optionalString(
                                key = "validationReason"
                            )
                    )
                }

        val duplicates =
            diagnostics
                .groupingBy {
                    it.catalogKey
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicates.isEmpty()) {
            "Duplicate nutrition match diagnostics: " +
                    duplicates
                        .sorted()
                        .joinToString()
        }

        return diagnostics.associateBy {
            it.catalogKey
        }
    }



    private fun readRepresentativeValidations(
        file: File
    ): Map<String, RepresentativeValidation> {

        val root =
            parseRoot(
                file = file
            )

        val version =
            root.requiredInt(
                key = "version",
                source = file.absolutePath
            )

        require(version == SOURCE_VERSION) {
            "Unsupported representative validation version: " +
                    version
        }

        val entries =
            root.requiredArray(
                key = "entries",
                source = file.absolutePath
            )
                .map { element ->

                    val json =
                        element.asJsonObject

                    RepresentativeValidation(
                        catalogKey =
                            normalizeKey(
                                json.requiredString(
                                    key = "catalogKey",
                                    source =
                                        "representative validation"
                                )
                            ),
                        selectedServerKey =
                            normalizeKey(
                                json.requiredString(
                                    key = "selectedServerKey",
                                    source =
                                        "representative validation"
                                )
                            ),
                        decisionType =
                            json.requiredString(
                                key = "decisionType",
                                source =
                                    "representative validation"
                            ),
                        reasons =
                            json.requiredStringArray(
                                key = "reasons",
                                source =
                                    "representative validation"
                            ),
                        accepted =
                            json.requiredBoolean(
                                key = "accepted",
                                source =
                                    "representative validation"
                            )
                    )
                }

        val duplicates =
            entries
                .groupingBy {
                    it.catalogKey
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicates.isEmpty()) {
            "Duplicate representative validations: " +
                    duplicates
                        .sorted()
                        .joinToString()
        }

        return entries.associateBy {
            it.catalogKey
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

    private fun writeDataset(
        dataset: NutritionMatcherTrainingDataset,
        outputFile: File
    ) {
        outputFile.parentFile
            ?.let { directory ->

                if (!directory.exists()) {
                    check(directory.mkdirs()) {
                        "Could not create matcher training " +
                                "directory: " +
                                directory.absolutePath
                    }
                }

                require(directory.isDirectory) {
                    "Matcher training parent path is not a " +
                            "directory: " +
                            directory.absolutePath
                }
            }

        val gson =
            GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()

        outputFile.writeText(
            gson.toJson(dataset) + "\n"
        )
    }

    private fun printDataset(
        dataset: NutritionMatcherTrainingDataset,
        outputFile: File,
        output: PrintStream
    ) {
        val summary =
            dataset.summary

        output.println()
        output.println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
        output.println(
            "NUTRITION MATCHER TRAINING DATASET"
        )
        output.println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
        output.println(
            "Catalog keys             : " +
                    summary.sourceCatalogKeyCount
        )
        output.println(
            "Examples                 : " +
                    summary.exampleCount
        )
        output.println(
            "Positive                 : " +
                    summary.positiveCount
        )
        output.println(
            "Accepted original        : " +
                    summary.acceptedOriginalMatchCount
        )
        output.println(
            "Negative                 : " +
                    summary.negativeCount
        )
        output.println()
        output.println(
            "Accepted selected        : " +
                    summary.acceptedSelectedCount
        )
        output.println(
            "Rejected selected        : " +
                    summary.rejectedSelectedCount
        )
        output.println(
            "NO_MATCH candidates      : " +
                    summary.rejectedNoMatchCandidateCount
        )
        output.println(
            "Alternative negatives    : " +
                    summary.nonSelectedAlternativeCount
        )
        output.println()
        output.println(
            "Output                   : " +
                    outputFile.absolutePath
        )
        output.println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
    }

    private fun parseRoot(
        file: File
    ): JsonObject {

        val element =
            JsonParser.parseString(
                file.readText()
            )

        require(element.isJsonObject) {
            "Expected JSON object in: " +
                    file.absolutePath
        }

        return element.asJsonObject
    }

    private fun JsonObject.requiredArray(
        key: String,
        source: String
    ): JsonArray {

        return get(key)
            ?.takeIf {
                it.isJsonArray
            }
            ?.asJsonArray
            ?: error(
                "Missing array '$key' in $source."
            )
    }

    private fun JsonObject.requiredOrderedStringArray(
        key: String,
        source: String
    ): List<String> {

        val values =
            requiredArray(
                key = key,
                source = source
            )
                .mapIndexed { index, element ->

                    require(
                        element.isJsonPrimitive &&
                                element.asJsonPrimitive.isString
                    ) {
                        "Expected string at '$key[$index]' " +
                                "in $source."
                    }

                    element.asString
                        .trim()
                        .also {
                            require(it.isNotBlank()) {
                                "Blank value at '$key[$index]' " +
                                        "in $source."
                            }
                        }
                }

        require(
            values.size ==
                    values.distinct().size
        ) {
            "Duplicate values in ordered array '$key' " +
                    "in $source."
        }

        return values
    }

    private fun JsonObject.requiredStringArray(
        key: String,
        source: String
    ): List<String> {

        return requiredArray(
            key = key,
            source = source
        )
            .mapIndexed { index, element ->

                require(
                    element.isJsonPrimitive &&
                            element.asJsonPrimitive.isString
                ) {
                    "Expected string at '$key[$index]' " +
                            "in $source."
                }

                element.asString
                    .trim()
                    .also {
                        require(it.isNotBlank()) {
                            "Blank value at '$key[$index]' " +
                                    "in $source."
                        }
                    }
            }
            .distinct()
            .sorted()
    }

    private fun JsonObject.requiredString(
        key: String,
        source: String
    ): String {

        return optionalString(key)
            ?: error(
                "Missing or blank string '$key' in $source."
            )
    }

    private fun JsonObject.optionalString(
        key: String
    ): String? {

        return get(key)
            ?.takeIf {
                !it.isJsonNull &&
                        it.isJsonPrimitive &&
                        it.asJsonPrimitive.isString
            }
            ?.asString
            ?.trim()
            ?.takeIf {
                it.isNotBlank()
            }
    }

    private fun JsonObject.requiredDouble(
        key: String,
        source: String
    ): Double {

        val element =
            get(key)

        require(
            element != null &&
                    !element.isJsonNull &&
                    element.isJsonPrimitive &&
                    element.asJsonPrimitive.isNumber
        ) {
            "Missing number '$key' in $source."
        }

        return element.asDouble
    }

    private fun JsonObject.requiredInt(
        key: String,
        source: String
    ): Int {

        val element =
            get(key)

        require(
            element != null &&
                    !element.isJsonNull &&
                    element.isJsonPrimitive &&
                    element.asJsonPrimitive.isNumber
        ) {
            "Missing integer '$key' in $source."
        }

        return element.asInt
    }

    private fun JsonObject.optionalInt(
        key: String
    ): Int? {

        val element =
            get(key)
                ?: return null

        if (
            element.isJsonNull ||
            !element.isJsonPrimitive ||
            !element.asJsonPrimitive.isNumber
        ) {
            return null
        }

        return element.asInt
    }

    private fun JsonObject.requiredBoolean(
        key: String,
        source: String
    ): Boolean {

        val element =
            get(key)

        require(
            element != null &&
                    !element.isJsonNull &&
                    element.isJsonPrimitive &&
                    element.asJsonPrimitive.isBoolean
        ) {
            "Missing boolean '$key' in $source."
        }

        return element.asBoolean
    }

    private fun JsonObject.optionalBoolean(
        key: String
    ): Boolean? {

        val element =
            get(key)
                ?: return null

        if (
            element.isJsonNull ||
            !element.isJsonPrimitive ||
            !element.asJsonPrimitive.isBoolean
        ) {
            return null
        }

        return element.asBoolean
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

    private fun readPersistedMappings(
        file: File
    ): Map<String, PersistedMapping> {

        val root =
            parseRoot(
                file = file
            )

        val mappings =
            root.requiredArray(
                key = "mappings",
                source = file.absolutePath
            )
                .map { element ->

                    val json =
                        element.asJsonObject

                    PersistedMapping(
                        catalogKey =
                            normalizeKey(
                                json.requiredString(
                                    key = "catalogKey",
                                    source =
                                        "catalog-server mapping"
                                )
                            ),
                        serverArtifact =
                            json.optionalString(
                                key = "serverArtifact"
                            )
                                ?: NUTRITION_ARTIFACT,
                        serverKey =
                            normalizeKey(
                                json.requiredString(
                                    key = "serverKey",
                                    source =
                                        "catalog-server mapping"
                                )
                            )
                    )
                }
                .filter {
                    it.serverArtifact ==
                            NUTRITION_ARTIFACT
                }

        val duplicates =
            mappings
                .groupingBy {
                    it.catalogKey
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicates.isEmpty()) {
            "Duplicate nutrition catalog-server mappings: " +
                    duplicates
                        .sorted()
                        .joinToString()
        }

        return mappings.associateBy {
            it.catalogKey
        }
    }

    private fun calculateSharedTokens(
        catalogKey: String,
        serverKey: String
    ): List<String> {

        val catalogTokens =
            tokenize(
                value = catalogKey
            )

        val serverTokens =
            tokenize(
                value = serverKey
            )

        return catalogTokens
            .intersect(
                serverTokens
            )
            .sorted()
    }

    private fun tokenize(
        value: String
    ): Set<String> {

        return normalizeKey(value)
            .split(" ")
            .asSequence()
            .map {
                it.trim()
            }
            .filter {
                it.isNotBlank()
            }
            .toSortedSet()
    }

    private data class PersistedMapping(
        val catalogKey: String,
        val serverArtifact: String,
        val serverKey: String
    )

    private data class CandidateQualityEntry(
        val catalogKey: String,
        val candidates:
        List<CandidateQualityCandidate>
    )

    private data class CandidateQualityCandidate(
        val rank: Int,
        val serverKey: String,
        val diagnosticScore: Double,
        val sharedTokens: List<String>,
        val selected: Boolean
    )

    private data class MatchDiagnostic(
        val catalogKey: String,
        val candidateCount: Int,
        val candidateServerKeys: List<String>,
        val decisionType: String,
        val selectedServerKey: String?,
        val confidence: Double,
        val decisionReason: String?,
        val validationStatus: String,
        val validationReason: String?,
        val mappingWritten: Boolean
    )

    private data class RepresentativeValidation(
        val catalogKey: String,
        val selectedServerKey: String,
        val decisionType: String,
        val reasons: List<String>,
        val accepted: Boolean
    )

    private data class Classification(
        val label: NutritionMatcherTrainingLabel,
        val role: NutritionMatcherTrainingExampleRole,
        val trainingWeight: Double
    )

    private data class CandidateIdentity(
        val catalogKey: String,
        val serverKey: String
    )

    private companion object {

        const val ACCEPTED =
            "ACCEPTED"

        const val MATCH =
            "MATCH"

        const val ORIGINAL_VALIDATOR =
            "CatalogServerMappingDecisionValidator"

        const val SOURCE_VERSION =
            1

        const val NUTRITION_ARTIFACT =
            "nutrition.json"

        const val DATASET_NAMESPACE =
            "nutrition-catalog-server-matcher-v1"

        const val SOURCE_TYPE =
            "GPT_5_5_MATCHER_WITH_DETERMINISTIC_VALIDATION"

        const val MATCHER =
            "GPT-5.5 CatalogToServerMatcher"

        const val VALIDATOR =
            "DeterministicRepresentativeNutritionMappingValidator"

        const val IDENTICAL =
            "IDENTICAL"

        const val REPRESENTATIVE =
            "REPRESENTATIVE"

        const val INCOMPATIBLE =
            "INCOMPATIBLE"

        const val REJECTED_NO_MATCH =
            "REJECTED_NO_MATCH"

        const val POSITIVE_WEIGHT =
            1.0

        const val REJECTED_SELECTED_WEIGHT =
            1.0

        const val NO_MATCH_NEGATIVE_WEIGHT =
            0.85

        const val ALTERNATIVE_NEGATIVE_WEIGHT =
            0.5

        val WHITESPACE_REGEX =
            Regex("\\s+")
    }
}