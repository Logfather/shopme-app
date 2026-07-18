package de.shopme.tools.knowledge.rebuild.nutrition.diagnostics

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.util.Locale

class RejectedStrongNutritionCandidateDiagnosticRunner(
    private val coverageGapReportFile: File,
    private val requestFile: File,
    private val decisionFile: File
) {

    fun run():
            RejectedStrongNutritionCandidateDiagnosticReport {

        require(coverageGapReportFile.isFile) {
            "Nutrition coverage-gap report does not exist: " +
                    coverageGapReportFile.absolutePath
        }

        require(requestFile.isFile) {
            "Nutrition match-request file does not exist: " +
                    requestFile.absolutePath
        }

        require(decisionFile.isFile) {
            "Nutrition match-decision file does not exist: " +
                    decisionFile.absolutePath
        }

        val selectedGaps =
            readSelectedGaps(
                file =
                    coverageGapReportFile
            )

        val requestsByCatalogKey =
            readRequests(
                file =
                    requestFile
            )
                .associateBy {
                    it.catalogKey
                }

        val decisionsByCatalogKey =
            readDecisions(
                file =
                    decisionFile
            )
                .associateBy {
                    it.catalogKey
                }

        val diagnostics =
            selectedGaps
                .map { gap ->

                    val request =
                        requestsByCatalogKey[
                            gap.catalogKey
                        ]
                            ?: error(
                                "No persisted request exists for " +
                                        "selected rejected candidate: " +
                                        gap.catalogKey
                            )

                    val decision =
                        decisionsByCatalogKey[
                            gap.catalogKey
                        ]
                            ?: error(
                                "No persisted decision exists for " +
                                        "selected rejected candidate: " +
                                        gap.catalogKey
                            )

                    require(
                        decision.type ==
                                NO_MATCH_TYPE
                    ) {
                        "Expected NO_MATCH decision for " +
                                "'${gap.catalogKey}', but was " +
                                "'${decision.type}'."
                    }

                    val topCandidate =
                        request.candidates.firstOrNull()
                            ?: error(
                                "Selected rejected candidate has no " +
                                        "persisted candidates: " +
                                        gap.catalogKey
                            )

                    require(
                        topCandidate.serverKey ==
                                gap.topCandidateKey
                    ) {
                        "Coverage report top candidate differs from " +
                                "request artifact for " +
                                "'${gap.catalogKey}': coverage=" +
                                "'${gap.topCandidateKey}', request=" +
                                "'${topCandidate.serverKey}'."
                    }

                    createDiagnostic(
                        gap =
                            gap,
                        request =
                            request,
                        decision =
                            decision
                    )
                }
                .sortedBy {
                    it.catalogKey
                }

        val report =
            RejectedStrongNutritionCandidateDiagnosticReport(
                version =
                    RejectedStrongNutritionCandidateDiagnosticReport
                        .CURRENT_VERSION,
                selectedGapCount =
                    diagnostics.size,
                strongTopCandidateCount =
                    diagnostics.count {
                        it.noMatchCause ==
                                STRONG_CAUSE
                    },
                moderateTopCandidateCount =
                    diagnostics.count {
                        it.noMatchCause ==
                                MODERATE_CAUSE
                    },
                representativeReviewRecommendedCount =
                    diagnostics.count {
                        it.representativeReviewRecommended
                    },
                conflictCount =
                    diagnostics.count {
                        it.diagnosticType in
                                RejectedStrongNutritionCandidateDiagnosticReport
                                    .CONFLICT_TYPES
                    },
                countsByDiagnosticType =
                    diagnostics
                        .groupingBy {
                            it.diagnosticType.name
                        }
                        .eachCount()
                        .toSortedMap(),
                diagnostics =
                    diagnostics
            )

        require(
            report.selectedGapCount ==
                    selectedGaps.size
        ) {
            "Diagnostic report does not cover every selected gap."
        }

        return report
    }

    private fun createDiagnostic(
        gap: PersistedGap,
        request: PersistedRequest,
        decision: PersistedDecision
    ): RejectedStrongNutritionCandidateDiagnostic {

        val topCandidate =
            request.candidates.first()

        val secondCandidate =
            request.candidates.getOrNull(
                index =
                    1
            )

        val catalogTokens =
            tokenize(
                value =
                    gap.catalogKey
            )

        val candidateTokens =
            tokenize(
                value =
                    topCandidate.serverKey
            )

        val catalogCoreTokens =
            coreTokens(
                tokens =
                    catalogTokens
            )

        val candidateCoreTokens =
            coreTokens(
                tokens =
                    candidateTokens
            )

        val sharedCoreTokens =
            catalogCoreTokens
                .intersect(
                    candidateCoreTokens
                )

        val missingCatalogCoreTokens =
            catalogCoreTokens
                .minus(
                    candidateCoreTokens
                )

        val additionalCandidateCoreTokens =
            candidateCoreTokens
                .minus(
                    catalogCoreTokens
                )

        val specializationRisk =
            classifySpecializationRisk(
                additionalCandidateCoreTokens =
                    additionalCandidateCoreTokens
            )

        val catalogProductClasses =
            productClasses(
                tokens =
                    catalogTokens
            )

        val candidateProductClasses =
            productClasses(
                tokens =
                    candidateTokens
            )

        val modifierConflicts =
            findConflicts(
                catalogTokens =
                    catalogTokens,
                candidateTokens =
                    candidateTokens,
                groups =
                    CRITICAL_MODIFIER_GROUPS
            )

        val processingStateConflicts =
            findConflicts(
                catalogTokens =
                    catalogTokens,
                candidateTokens =
                    candidateTokens,
                groups =
                    PROCESSING_STATE_GROUPS
            )

        val productFormConflicts =
            findConflicts(
                catalogTokens =
                    catalogTokens,
                candidateTokens =
                    candidateTokens,
                groups =
                    PRODUCT_FORM_GROUPS
            )

        val classification =
            classify(
                catalogCoreTokens =
                    catalogCoreTokens,
                candidateCoreTokens =
                    candidateCoreTokens,
                sharedCoreTokens =
                    sharedCoreTokens,
                missingCatalogCoreTokens =
                    missingCatalogCoreTokens,
                additionalCandidateCoreTokens =
                    additionalCandidateCoreTokens,
                specializationRisk =
                    specializationRisk,
                catalogProductClasses =
                    catalogProductClasses,
                candidateProductClasses =
                    candidateProductClasses,
                modifierConflicts =
                    modifierConflicts,
                processingStateConflicts =
                    processingStateConflicts,
                productFormConflicts =
                    productFormConflicts,
                topCandidateScore =
                    topCandidate.diagnosticScore
            )

        return RejectedStrongNutritionCandidateDiagnostic(
            catalogKey =
                gap.catalogKey,
            noMatchCause =
                gap.noMatchCause,
            topCandidateKey =
                topCandidate.serverKey,
            topCandidateScore =
                topCandidate.diagnosticScore,
            topCandidateRank =
                1,
            topCandidateSharedTokens =
                topCandidate.sharedTokens
                    .distinct()
                    .sorted(),
            specializationRiskTypes =
                specializationRisk.types
                    .sortedBy {
                        it.name
                    },
            highRiskAdditionalTokens =
                specializationRisk.highRiskTokens
                    .sorted(),
            unknownAdditionalTokens =
                specializationRisk.unknownTokens
                    .sorted(),
            secondCandidateScore =
                secondCandidate?.diagnosticScore,
            topScoreDelta =
                secondCandidate
                    ?.let {
                        (
                                topCandidate.diagnosticScore -
                                        it.diagnosticScore
                                )
                            .coerceAtLeast(
                                minimumValue =
                                    0.0
                            )
                    },
            decisionConfidence =
                decision.confidence,
            decisionSource =
                decision.decisionSource,
            decisionReason =
                decision.reason,
            catalogTokens =
                catalogTokens.sorted(),
            candidateTokens =
                candidateTokens.sorted(),
            sharedCoreTokens =
                sharedCoreTokens.sorted(),
            missingCatalogCoreTokens =
                missingCatalogCoreTokens.sorted(),
            additionalCandidateCoreTokens =
                additionalCandidateCoreTokens.sorted(),
            catalogProductClasses =
                catalogProductClasses.sorted(),
            candidateProductClasses =
                candidateProductClasses.sorted(),
            modifierConflicts =
                modifierConflicts,
            processingStateConflicts =
                processingStateConflicts,
            productFormConflicts =
                productFormConflicts,
            diagnosticType =
                classification.type,
            representativeReviewRecommended =
                classification.representativeReviewRecommended,
            details =
                classification.details
        )
    }

    private fun classify(
        catalogCoreTokens: Set<String>,
        candidateCoreTokens: Set<String>,
        sharedCoreTokens: Set<String>,
        missingCatalogCoreTokens: Set<String>,
        additionalCandidateCoreTokens: Set<String>,
        specializationRisk: SpecializationRisk,
        catalogProductClasses: Set<String>,
        candidateProductClasses: Set<String>,
        modifierConflicts: List<String>,
        processingStateConflicts: List<String>,
        productFormConflicts: List<String>,
        topCandidateScore: Double
    ): Classification {

        if (processingStateConflicts.isNotEmpty()) {
            return Classification(
                type =
                    RejectedStrongNutritionCandidateDiagnosticType
                        .PROCESSING_STATE_CONFLICT,
                representativeReviewRecommended =
                    false,
                details =
                    "Catalog key and candidate contain conflicting " +
                            "processing states: " +
                            processingStateConflicts.joinToString() +
                            "."
            )
        }

        if (productFormConflicts.isNotEmpty()) {
            return Classification(
                type =
                    RejectedStrongNutritionCandidateDiagnosticType
                        .PRODUCT_FORM_CONFLICT,
                representativeReviewRecommended =
                    false,
                details =
                    "Catalog key and candidate contain conflicting " +
                            "product forms: " +
                            productFormConflicts.joinToString() +
                            "."
            )
        }

        if (modifierConflicts.isNotEmpty()) {
            return Classification(
                type =
                    RejectedStrongNutritionCandidateDiagnosticType
                        .CRITICAL_MODIFIER_CONFLICT,
                representativeReviewRecommended =
                    false,
                details =
                    "Catalog key and candidate contain conflicting " +
                            "critical modifiers: " +
                            modifierConflicts.joinToString() +
                            "."
            )
        }

        if (
            catalogProductClasses.isNotEmpty() &&
            candidateProductClasses.isNotEmpty() &&
            catalogProductClasses.intersect(
                candidateProductClasses
            ).isEmpty()
        ) {
            return Classification(
                type =
                    RejectedStrongNutritionCandidateDiagnosticType
                        .DIFFERENT_PRODUCT_CLASS,
                representativeReviewRecommended =
                    false,
                details =
                    "Catalog key and candidate belong to different " +
                            "product classes: catalog=" +
                            catalogProductClasses.sorted().joinToString() +
                            ", candidate=" +
                            candidateProductClasses.sorted().joinToString() +
                            "."
            )
        }

        if (
            specializationRisk.highRiskTypes
                .isNotEmpty()
        ) {
            return Classification(
                type =
                    RejectedStrongNutritionCandidateDiagnosticType
                        .BRAND_OR_VARIANT_MISMATCH,
                representativeReviewRecommended =
                    false,
                details =
                    "Candidate adds nutrition-relevant specialization " +
                            "tokens: tokens=" +
                            specializationRisk.highRiskTokens
                                .sorted()
                                .joinToString() +
                            ", riskTypes=" +
                            specializationRisk.highRiskTypes
                                .map {
                                    it.name
                                }
                                .sorted()
                                .joinToString() +
                            "."
            )
        }

        if (
            specializationRisk.unknownTokens
                .isNotEmpty()
        ) {
            return Classification(
                type =
                    RejectedStrongNutritionCandidateDiagnosticType
                        .INSUFFICIENT_SEMANTIC_EVIDENCE,
                representativeReviewRecommended =
                    false,
                details =
                    "Candidate adds unclassified specialization tokens: " +
                            specializationRisk.unknownTokens
                                .sorted()
                                .joinToString() +
                            "."
            )
        }

        if (
            catalogCoreTokens.isEmpty() ||
            candidateCoreTokens.isEmpty() ||
            sharedCoreTokens.isEmpty()
        ) {
            return Classification(
                type =
                    RejectedStrongNutritionCandidateDiagnosticType
                        .INSUFFICIENT_SEMANTIC_EVIDENCE,
                representativeReviewRecommended =
                    false,
                details =
                    "Catalog key and candidate do not share sufficient " +
                            "core semantic evidence."
            )
        }

        if (
            missingCatalogCoreTokens.isEmpty() &&
            additionalCandidateCoreTokens.isEmpty()
        ) {
            return Classification(
                type =
                    RejectedStrongNutritionCandidateDiagnosticType
                        .LIKELY_REPRESENTATIVE,
                representativeReviewRecommended =
                    true,
                details =
                    "Catalog key and candidate share the same core " +
                            "semantic tokens without a detected critical " +
                            "conflict."
            )
        }

        if (
            missingCatalogCoreTokens.isEmpty() &&
            additionalCandidateCoreTokens.isNotEmpty()
        ) {
            val nonCriticalAdditions =
                specializationRisk.types.isNotEmpty() &&
                        specializationRisk.types.all {
                            it ==
                                    NutritionSpecializationRiskType
                                        .NON_CRITICAL_STYLE
                        }

            return if (nonCriticalAdditions) {
                Classification(
                    type =
                        RejectedStrongNutritionCandidateDiagnosticType
                            .ADDITIONAL_NON_CRITICAL_MODIFIER,
                    representativeReviewRecommended =
                        true,
                    details =
                        "Candidate adds only non-critical modifiers: " +
                                additionalCandidateCoreTokens
                                    .sorted()
                                    .joinToString() +
                                "."
                )
            } else {
                Classification(
                    type =
                        RejectedStrongNutritionCandidateDiagnosticType
                            .COMPATIBLE_SPECIALIZATION,
                    representativeReviewRecommended =
                        topCandidateScore >=
                                MIN_REVIEW_SPECIALIZATION_SCORE,
                    details =
                        "Candidate is a more specific variant of the " +
                                "catalog product: additionalCoreTokens=" +
                                additionalCandidateCoreTokens
                                    .sorted()
                                    .joinToString() +
                                "."
                )
            }
        }

        if (
            missingCatalogCoreTokens.isNotEmpty() &&
            additionalCandidateCoreTokens.isEmpty()
        ) {
            val missingOnlyNonCritical =
                missingCatalogCoreTokens
                    .all {
                        it in
                                NON_CRITICAL_VARIANT_TOKENS
                    }

            return if (missingOnlyNonCritical) {
                Classification(
                    type =
                        RejectedStrongNutritionCandidateDiagnosticType
                            .MISSING_NON_CRITICAL_MODIFIER,
                    representativeReviewRecommended =
                        true,
                    details =
                        "Candidate omits only non-critical modifiers: " +
                                missingCatalogCoreTokens
                                    .sorted()
                                    .joinToString() +
                                "."
                )
            } else {
                Classification(
                    type =
                        RejectedStrongNutritionCandidateDiagnosticType
                            .COMPATIBLE_GENERALIZATION,
                    representativeReviewRecommended =
                        topCandidateScore >=
                                MIN_REVIEW_GENERALIZATION_SCORE,
                    details =
                        "Candidate is a broader variant of the catalog " +
                                "product: missingCatalogCoreTokens=" +
                                missingCatalogCoreTokens
                                    .sorted()
                                    .joinToString() +
                                "."
                )
            }
        }

        return Classification(
            type =
                RejectedStrongNutritionCandidateDiagnosticType
                    .INSUFFICIENT_SEMANTIC_EVIDENCE,
            representativeReviewRecommended =
                false,
            details =
                "Catalog key and candidate contain both missing and " +
                        "additional core semantic tokens: missing=" +
                        missingCatalogCoreTokens.sorted().joinToString() +
                        ", additional=" +
                        additionalCandidateCoreTokens
                            .sorted()
                            .joinToString() +
                        "."
        )
    }

    private fun readSelectedGaps(
        file: File
    ): List<PersistedGap> {

        val root =
            parseObject(
                file =
                    file
            )

        return root.requiredArray(
            key =
                "gaps"
        )
            .mapNotNull { element ->

                require(element.isJsonObject) {
                    "Coverage gap entry must be a JSON object."
                }

                val gap =
                    element.asJsonObject

                val type =
                    gap.requiredString(
                        key =
                            "type"
                    )

                val noMatchCause =
                    gap.optionalString(
                        key =
                            "noMatchCause"
                    )

                if (
                    type !=
                    NO_MATCH_TYPE ||
                    noMatchCause !in
                    SUPPORTED_CAUSES
                ) {
                    return@mapNotNull null
                }

                PersistedGap(
                    catalogKey =
                        normalizeKey(
                            gap.requiredString(
                                key =
                                    "catalogKey"
                            )
                        ),
                    noMatchCause =
                        requireNotNull(
                            noMatchCause
                        ),
                    topCandidateKey =
                        normalizeKey(
                            gap.requiredString(
                                key =
                                    "topCandidateKey"
                            )
                        )
                )
            }
            .also { gaps ->

                requireNoDuplicateCatalogKeys(
                    values =
                        gaps.map {
                            it.catalogKey
                        },
                    sourceName =
                        "selected rejected strong nutrition gaps"
                )
            }
            .sortedBy {
                it.catalogKey
            }
    }

    private fun readRequests(
        file: File
    ): List<PersistedRequest> {

        val root =
            parseObject(
                file =
                    file
            )

        return root.requiredArray(
            key =
                "requests"
        )
            .mapNotNull { element ->

                require(element.isJsonObject) {
                    "Match request entry must be a JSON object."
                }

                val request =
                    element.asJsonObject

                if (
                    request.requiredString(
                        key =
                            "serverArtifact"
                    ) !=
                    NUTRITION_ARTIFACT
                ) {
                    return@mapNotNull null
                }

                PersistedRequest(
                    catalogKey =
                        normalizeKey(
                            request.requiredString(
                                key =
                                    "catalogKey"
                            )
                        ),
                    candidates =
                        request.requiredArray(
                            key =
                                "candidates"
                        )
                            .map { candidateElement ->

                                require(candidateElement.isJsonObject) {
                                    "Match candidate must be a JSON " +
                                            "object."
                                }

                                val candidate =
                                    candidateElement.asJsonObject

                                PersistedCandidate(
                                    serverKey =
                                        normalizeKey(
                                            candidate.requiredString(
                                                key =
                                                    "serverKey"
                                            )
                                        ),
                                    diagnosticScore =
                                        candidate.requiredDouble(
                                            key =
                                                "diagnosticScore"
                                        ),
                                    sharedTokens =
                                        candidate.requiredArray(
                                            key =
                                                "sharedTokens"
                                        )
                                            .map { tokenElement ->

                                                require(
                                                    tokenElement
                                                        .isJsonPrimitive &&
                                                            tokenElement
                                                                .asJsonPrimitive
                                                                .isString
                                                ) {
                                                    "sharedTokens must " +
                                                            "contain " +
                                                            "strings."
                                                }

                                                normalizeKey(
                                                    tokenElement.asString
                                                )
                                            }
                                            .filter {
                                                it.isNotBlank()
                                            }
                                            .distinct()
                                            .sorted()
                                )
                            }
                )
            }
            .also { requests ->

                requireNoDuplicateCatalogKeys(
                    values =
                        requests.map {
                            it.catalogKey
                        },
                    sourceName =
                        "nutrition match requests"
                )
            }
    }

    private fun readDecisions(
        file: File
    ): List<PersistedDecision> {

        val root =
            parseObject(
                file =
                    file
            )

        return root.requiredArray(
            key =
                "decisions"
        )
            .mapNotNull { element ->

                require(element.isJsonObject) {
                    "Match decision entry must be a JSON object."
                }

                val decision =
                    element.asJsonObject

                if (
                    decision.requiredString(
                        key =
                            "serverArtifact"
                    ) !=
                    NUTRITION_ARTIFACT
                ) {
                    return@mapNotNull null
                }

                PersistedDecision(
                    catalogKey =
                        normalizeKey(
                            decision.requiredString(
                                key =
                                    "catalogKey"
                            )
                        ),
                    type =
                        decision.requiredString(
                            key =
                                "type"
                        ),
                    confidence =
                        decision.requiredDouble(
                            key =
                                "confidence"
                        ),
                    reason =
                        decision.requiredString(
                            key =
                                "reason"
                        ),
                    decisionSource =
                        decision.optionalString(
                            key =
                                "decisionSource"
                        )
                            ?: DEFAULT_DECISION_SOURCE
                )
            }
            .also { decisions ->

                requireNoDuplicateCatalogKeys(
                    values =
                        decisions.map {
                            it.catalogKey
                        },
                    sourceName =
                        "nutrition match decisions"
                )
            }
    }

    private fun classifySpecializationRisk(
        additionalCandidateCoreTokens: Set<String>
    ): SpecializationRisk {

        if (additionalCandidateCoreTokens.isEmpty()) {
            return SpecializationRisk(
                types =
                    emptySet(),
                highRiskTypes =
                    emptySet(),
                highRiskTokens =
                    emptySet(),
                unknownTokens =
                    emptySet()
            )
        }

        val types =
            sortedSetOf<NutritionSpecializationRiskType>(
                compareBy {
                    it.name
                }
            )

        val highRiskTypes =
            sortedSetOf<NutritionSpecializationRiskType>(
                compareBy {
                    it.name
                }
            )

        val highRiskTokens =
            sortedSetOf<String>()

        val unknownTokens =
            sortedSetOf<String>()

        additionalCandidateCoreTokens
            .sorted()
            .forEach { token ->

                val riskType =
                    SPECIALIZATION_TOKEN_TYPES[
                        token
                    ]
                        ?: NutritionSpecializationRiskType
                            .UNKNOWN_ADDITIONAL_TOKEN

                types +=
                    riskType

                when (riskType) {

                    NutritionSpecializationRiskType
                        .NON_CRITICAL_STYLE -> {
                        // Kein hohes Nutrition-Risiko.
                    }

                    NutritionSpecializationRiskType
                        .UNKNOWN_ADDITIONAL_TOKEN -> {

                        unknownTokens +=
                            token
                    }

                    else -> {
                        highRiskTypes +=
                            riskType

                        highRiskTokens +=
                            token
                    }
                }
            }

        return SpecializationRisk(
            types =
                types,
            highRiskTypes =
                highRiskTypes,
            highRiskTokens =
                highRiskTokens,
            unknownTokens =
                unknownTokens
        )
    }

    private fun productClasses(
        tokens: Set<String>
    ): Set<String> {

        return PRODUCT_CLASS_TOKENS
            .mapNotNull { (productClass, classTokens) ->

                productClass.takeIf {
                    tokens.intersect(
                        classTokens
                    ).isNotEmpty()
                }
            }
            .toSortedSet()
    }

    private fun coreTokens(
        tokens: Set<String>
    ): Set<String> {

        return tokens
            .asSequence()
            .filterNot {
                it in
                        NON_CORE_TOKENS
            }
            .toSortedSet()
    }

    private fun findConflicts(
        catalogTokens: Set<String>,
        candidateTokens: Set<String>,
        groups: List<Set<String>>
    ): List<String> {

        return groups
            .mapNotNull { group ->

                val catalogValues =
                    catalogTokens.intersect(
                        group
                    )

                val candidateValues =
                    candidateTokens.intersect(
                        group
                    )

                if (
                    catalogValues.isNotEmpty() &&
                    candidateValues.isNotEmpty() &&
                    catalogValues !=
                    candidateValues
                ) {
                    (
                            catalogValues.sorted()
                                .joinToString(
                                    separator =
                                        "+"
                                ) +
                                    " vs " +
                                    candidateValues.sorted()
                                        .joinToString(
                                            separator =
                                                "+"
                                        )
                            )
                } else {
                    null
                }
            }
            .distinct()
            .sorted()
    }

    private fun tokenize(
        value: String
    ): Set<String> {

        return normalizeKey(
            value =
                value
        )
            .split(
                " "
            )
            .filter {
                it.isNotBlank()
            }
            .toSortedSet()
    }

    private fun normalizeKey(
        value: String
    ): String {

        return value
            .trim()
            .lowercase(
                Locale.ROOT
            )
            .replace(
                NON_ALPHANUMERIC_REGEX,
                " "
            )
            .replace(
                WHITESPACE_REGEX,
                " "
            )
            .trim()
    }

    private fun parseObject(
        file: File
    ): JsonObject {

        val root =
            JsonParser.parseString(
                file.readText()
            )

        require(root.isJsonObject) {
            "Expected JSON object in: " +
                    file.absolutePath
        }

        return root.asJsonObject
    }

    private fun requireNoDuplicateCatalogKeys(
        values: List<String>,
        sourceName: String
    ) {
        val duplicates =
            values
                .groupingBy {
                    it
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicates.isEmpty()) {
            "$sourceName contain duplicate catalog keys: " +
                    duplicates
                        .sorted()
                        .take(MAX_DIAGNOSTIC_KEYS)
                        .joinToString()
        }
    }

    private fun JsonObject.requiredArray(
        key: String
    ): JsonArray {

        return get(key)
            ?.takeIf {
                it.isJsonArray
            }
            ?.asJsonArray
            ?: error(
                "Missing JSON array '$key'."
            )
    }

    private fun JsonObject.requiredString(
        key: String
    ): String {

        return optionalString(
            key =
                key
        )
            ?: error(
                "Missing or blank string '$key'."
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
        key: String
    ): Double {

        return get(key)
            ?.takeIf {
                it.isJsonPrimitive &&
                        it.asJsonPrimitive.isNumber
            }
            ?.asDouble
            ?: error(
                "Missing numeric value '$key'."
            )
    }

    private data class PersistedGap(
        val catalogKey: String,
        val noMatchCause: String,
        val topCandidateKey: String
    )

    private data class PersistedRequest(
        val catalogKey: String,
        val candidates: List<PersistedCandidate>
    )

    private data class PersistedCandidate(
        val serverKey: String,
        val diagnosticScore: Double,
        val sharedTokens: List<String>
    )

    private data class PersistedDecision(
        val catalogKey: String,
        val type: String,
        val confidence: Double,
        val reason: String,
        val decisionSource: String
    )

    private data class SpecializationRisk(
        val types: Set<NutritionSpecializationRiskType>,
        val highRiskTypes:
        Set<NutritionSpecializationRiskType>,
        val highRiskTokens: Set<String>,
        val unknownTokens: Set<String>
    ) {

        init {
            require(
                highRiskTypes.all {
                    it in
                            types
                }
            ) {
                "highRiskTypes must be contained in types."
            }

            require(
                NutritionSpecializationRiskType
                    .NON_CRITICAL_STYLE !in
                        highRiskTypes
            ) {
                "NON_CRITICAL_STYLE must not be high risk."
            }

            require(
                NutritionSpecializationRiskType
                    .UNKNOWN_ADDITIONAL_TOKEN !in
                        highRiskTypes
            ) {
                "UNKNOWN_ADDITIONAL_TOKEN must not be high risk."
            }
        }
    }

    private data class Classification(
        val type:
        RejectedStrongNutritionCandidateDiagnosticType,
        val representativeReviewRecommended: Boolean,
        val details: String
    )

    private companion object {

        const val NUTRITION_ARTIFACT =
            "nutrition.json"

        const val NO_MATCH_TYPE =
            "NO_MATCH"

        const val STRONG_CAUSE =
            "STRONG_TOP_CANDIDATE_REJECTED"

        const val MODERATE_CAUSE =
            "MODERATE_TOP_CANDIDATE_REJECTED"

        const val DEFAULT_DECISION_SOURCE =
            "CHAT_GPT"

        const val MIN_REVIEW_SPECIALIZATION_SCORE =
            0.80

        const val MIN_REVIEW_GENERALIZATION_SCORE =
            0.75

        const val MAX_DIAGNOSTIC_KEYS =
            10

        val SUPPORTED_CAUSES =
            setOf(
                STRONG_CAUSE,
                MODERATE_CAUSE
            )

        val NON_ALPHANUMERIC_REGEX =
            Regex("[^\\p{L}\\p{N}]+")

        val WHITESPACE_REGEX =
            Regex("\\s+")

        val NON_CORE_TOKENS =
            setOf(
                "and",
                "the",
                "of",
                "with",
                "style",
                "classic",
                "original"
            )

        val NON_CRITICAL_VARIANT_TOKENS =
            setOf(
                "fine",
                "wide",
                "milano",
                "mediterranean",
                "cultured",
                "summer",
                "french",
                "hearty",
                "traditional"
            )

        val SPECIALIZATION_TOKEN_TYPES =
            mapOf(
                /*
                 * Nutrition formulation
                 */
                "protein" to
                        NutritionSpecializationRiskType
                            .NUTRITION_FORMULATION,
                "highprotein" to
                        NutritionSpecializationRiskType
                            .NUTRITION_FORMULATION,
                "fortified" to
                        NutritionSpecializationRiskType
                            .NUTRITION_FORMULATION,
                "enriched" to
                        NutritionSpecializationRiskType
                            .NUTRITION_FORMULATION,
                "light" to
                        NutritionSpecializationRiskType
                            .NUTRITION_FORMULATION,
                "reduced" to
                        NutritionSpecializationRiskType
                            .NUTRITION_FORMULATION,

                /*
                 * Ingredient or substrate
                 */
                "edamame" to
                        NutritionSpecializationRiskType
                            .INGREDIENT_OR_SUBSTRATE,
                "soy" to
                        NutritionSpecializationRiskType
                            .INGREDIENT_OR_SUBSTRATE,
                "peanut" to
                        NutritionSpecializationRiskType
                            .INGREDIENT_OR_SUBSTRATE,
                "chocolate" to
                        NutritionSpecializationRiskType
                            .INGREDIENT_OR_SUBSTRATE,
                "seafood" to
                        NutritionSpecializationRiskType
                            .INGREDIENT_OR_SUBSTRATE,
                "cereal" to
                        NutritionSpecializationRiskType
                            .INGREDIENT_OR_SUBSTRATE,

                /*
                 * Species or subtype
                 */
                "white" to
                        NutritionSpecializationRiskType
                            .SPECIES_OR_SUBTYPE,
                "feta" to
                        NutritionSpecializationRiskType
                            .SPECIES_OR_SUBTYPE,
                "kidney" to
                        NutritionSpecializationRiskType
                            .SPECIES_OR_SUBTYPE,
                "french" to
                        NutritionSpecializationRiskType
                            .SPECIES_OR_SUBTYPE,
                "italian" to
                        NutritionSpecializationRiskType
                            .SPECIES_OR_SUBTYPE,

                /*
                 * Flavor
                 */
                "strawberry" to
                        NutritionSpecializationRiskType
                            .FLAVOR,
                "raspberry" to
                        NutritionSpecializationRiskType
                            .FLAVOR,
                "honey" to
                        NutritionSpecializationRiskType
                            .FLAVOR,
                "moccha" to
                        NutritionSpecializationRiskType
                            .FLAVOR,
                "mocha" to
                        NutritionSpecializationRiskType
                            .FLAVOR,
                "vanilla" to
                        NutritionSpecializationRiskType
                            .FLAVOR,

                /*
                 * Regional or independent identity
                 */
                "yangjiang" to
                        NutritionSpecializationRiskType
                            .REGIONAL_OR_PRODUCT_IDENTITY,
                "milano" to
                        NutritionSpecializationRiskType
                            .REGIONAL_OR_PRODUCT_IDENTITY,
                "mediterranean" to
                        NutritionSpecializationRiskType
                            .REGIONAL_OR_PRODUCT_IDENTITY,

                /*
                 * Product form
                 */
                "fusili" to
                        NutritionSpecializationRiskType
                            .PRODUCT_FORM,
                "fusilli" to
                        NutritionSpecializationRiskType
                            .PRODUCT_FORM,
                "bears" to
                        NutritionSpecializationRiskType
                            .PRODUCT_FORM,
                "biscuits" to
                        NutritionSpecializationRiskType
                            .PRODUCT_FORM,
                "sandwich" to
                        NutritionSpecializationRiskType
                            .PRODUCT_FORM,
                "fries" to
                        NutritionSpecializationRiskType
                            .PRODUCT_FORM,
                "gravy" to
                        NutritionSpecializationRiskType
                            .PRODUCT_FORM,
                "seasoning" to
                        NutritionSpecializationRiskType
                            .PRODUCT_FORM,
                "latte" to
                        NutritionSpecializationRiskType
                            .PRODUCT_FORM,

                /*
                 * Processing method
                 */
                "hot" to
                        NutritionSpecializationRiskType
                            .PROCESSING_METHOD,
                "cold" to
                        NutritionSpecializationRiskType
                            .PROCESSING_METHOD,
                "cultured" to
                        NutritionSpecializationRiskType
                            .PROCESSING_METHOD,
                "fermented" to
                        NutritionSpecializationRiskType
                            .PROCESSING_METHOD,

                /*
                 * Probably harmless for Nutrition identity
                 */
                "wide" to
                        NutritionSpecializationRiskType
                            .NON_CRITICAL_STYLE,
                "fine" to
                        NutritionSpecializationRiskType
                            .NON_CRITICAL_STYLE,
                "traditional" to
                        NutritionSpecializationRiskType
                            .NON_CRITICAL_STYLE,
                "classic" to
                        NutritionSpecializationRiskType
                            .NON_CRITICAL_STYLE,
                "original" to
                        NutritionSpecializationRiskType
                            .NON_CRITICAL_STYLE
            )

        val PRODUCT_CLASS_TOKENS =
            linkedMapOf(
                "bread" to
                        setOf(
                            "bread",
                            "roll",
                            "pita",
                            "toast",
                            "baguette"
                        ),
                "butter" to
                        setOf(
                            "butter"
                        ),
                "cheese" to
                        setOf(
                            "cheese",
                            "feta"
                        ),
                "drink" to
                        setOf(
                            "drink",
                            "juice",
                            "smoothie",
                            "coffee",
                            "tea",
                            "latte"
                        ),
                "fish" to
                        setOf(
                            "fish",
                            "salmon",
                            "trout",
                            "cod",
                            "pollock",
                            "tuna",
                            "herring"
                        ),
                "meat" to
                        setOf(
                            "meat",
                            "beef",
                            "pork",
                            "chicken",
                            "poultry",
                            "ham",
                            "sausage",
                            "salami",
                            "rabbit"
                        ),
                "pasta" to
                        setOf(
                            "pasta",
                            "noodles",
                            "spaghetti",
                            "lasagna",
                            "fusili"
                        ),
                "sauce" to
                        setOf(
                            "sauce",
                            "dressing",
                            "mustard"
                        ),
                "spread" to
                        setOf(
                            "spread",
                            "paste",
                            "pate"
                        ),
                "sweet" to
                        setOf(
                            "candy",
                            "chocolate",
                            "licorice",
                            "pudding",
                            "biscuits"
                        ),
                "vegetable" to
                        setOf(
                            "vegetable",
                            "beans",
                            "pepper",
                            "peppers",
                            "spinach",
                            "zucchini",
                            "kohlrabi"
                        ),
                "yogurt" to
                        setOf(
                            "yogurt",
                            "yoghurt",
                            "whey"
                        )
            )

        val CRITICAL_MODIFIER_GROUPS =
            listOf(
                setOf(
                    "vegan",
                    "vegetarian",
                    "meat",
                    "poultry",
                    "seafood"
                ),
                setOf(
                    "sugar",
                    "sugarfree",
                    "sweetened",
                    "unsweetened"
                ),
                setOf(
                    "organic",
                    "conventional"
                )
            )

        val PROCESSING_STATE_GROUPS =
            listOf(
                setOf(
                    "fresh",
                    "frozen",
                    "canned",
                    "dried",
                    "preserved"
                ),
                setOf(
                    "raw",
                    "cooked",
                    "fried",
                    "baked",
                    "roasted",
                    "smoked"
                )
            )

        val PRODUCT_FORM_GROUPS =
            listOf(
                setOf(
                    "whole",
                    "sliced",
                    "slices",
                    "ground",
                    "powder",
                    "fillet",
                    "leaves"
                ),
                setOf(
                    "pasta",
                    "seed",
                    "bread",
                    "roll",
                    "sauce",
                    "spread"
                )
            )
    }
}