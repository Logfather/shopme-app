package de.shopme.tools.knowledge.report

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import de.shopme.tools.knowledge.mapping.catalog.representative.DeterministicRepresentativeNutritionMappingValidator
import de.shopme.tools.knowledge.mapping.catalog.representative.RepresentativeNutritionMappingDecisionType
import de.shopme.tools.knowledge.mapping.catalog.representative.RepresentativeNutritionMappingRequest
import java.io.File
import java.io.PrintStream

class RejectedLowConfidenceNutritionMappingValidator(
    private val representativeValidator:
    DeterministicRepresentativeNutritionMappingValidator =
        DeterministicRepresentativeNutritionMappingValidator()
) {

    fun run(
        candidateQualityFile: File,
        diagnosticsFile: File,
        outputFile: File,
        output: PrintStream = System.out
    ): RejectedLowConfidenceNutritionValidationResult {

        require(candidateQualityFile.isFile) {
            "Rejected nutrition candidate quality file " +
                    "does not exist: " +
                    candidateQualityFile.absolutePath
        }

        require(diagnosticsFile.isFile) {
            "Nutrition match diagnostics file does not exist: " +
                    diagnosticsFile.absolutePath
        }

        val selectedCandidateRanks =
            readSelectedCandidateRanks(
                candidateQualityFile =
                    candidateQualityFile
            )

        val diagnostics =
            readDiagnostics(
                diagnosticsFile =
                    diagnosticsFile
            )

        val entries =
            diagnostics
                .asSequence()
                .filter {
                    it.validationStatus ==
                            REJECTED_LOW_CONFIDENCE
                }
                .map { diagnostic ->

                    validateDiagnostic(
                        diagnostic =
                            diagnostic,
                        selectedCandidateRanks =
                            selectedCandidateRanks
                    )
                }
                .sortedWith(
                    compareBy<
                            RejectedLowConfidenceNutritionValidationEntry
                            >(
                        { it.catalogKey },
                        { it.selectedServerKey }
                    )
                )
                .toList()

        val report =
            RejectedLowConfidenceNutritionValidationReport(
                summary =
                    createSummary(
                        entries =
                            entries
                    ),
                entries =
                    entries
            )

        writeReport(
            report =
                report,
            outputFile =
                outputFile
        )

        printReport(
            report =
                report,
            outputFile =
                outputFile,
            output =
                output
        )

        return RejectedLowConfidenceNutritionValidationResult(
            report =
                report,
            outputFile =
                outputFile.absolutePath
        )
    }

    private fun validateDiagnostic(
        diagnostic: MatchDiagnostic,
        selectedCandidateRanks:
        Map<CandidateIdentity, Int>
    ): RejectedLowConfidenceNutritionValidationEntry {

        val selectedServerKey =
            requireNotNull(
                diagnostic.selectedServerKey
                    ?.trim()
                    ?.takeIf {
                        it.isNotBlank()
                    }
            ) {
                "Rejected low-confidence diagnostic has no " +
                        "selectedServerKey: " +
                        diagnostic.catalogKey
            }

        val candidateIdentity =
            CandidateIdentity(
                catalogKey =
                    diagnostic.catalogKey,
                serverKey =
                    selectedServerKey
            )

        val candidateRank =
            selectedCandidateRanks[candidateIdentity]
                ?: error(
                    "Selected nutrition candidate rank was not " +
                            "found in rejected candidate quality report: " +
                            "${diagnostic.catalogKey} -> " +
                            selectedServerKey
                )

        val request =
            RepresentativeNutritionMappingRequest(
                catalogKey =
                    diagnostic.catalogKey,
                serverKey =
                    selectedServerKey,
                candidateRank =
                    candidateRank,
                confidence =
                    diagnostic.confidence
            )

        val validation =
            representativeValidator.validate(
                request =
                    request
            )

        val decisionType =
            validation.type

        val accepted =
            decisionType ==
                    RepresentativeNutritionMappingDecisionType.IDENTICAL ||
                    decisionType ==
                    RepresentativeNutritionMappingDecisionType.REPRESENTATIVE

        return RejectedLowConfidenceNutritionValidationEntry(
            catalogKey =
                diagnostic.catalogKey,
            selectedServerKey =
                selectedServerKey,
            candidateRank =
                candidateRank,
            originalConfidence =
                diagnostic.confidence,
            originalDecisionReason =
                diagnostic.decisionReason,
            originalValidationStatus =
                diagnostic.validationStatus,
            originalValidationReason =
                diagnostic.validationReason,
            decisionType =
                decisionType,
            reasons =
                validation.reasons
                    .distinct()
                    .sortedBy {
                        it.name
                    },
            accepted =
                accepted
        )
    }

    private fun createSummary(
        entries:
        List<RejectedLowConfidenceNutritionValidationEntry>
    ): RejectedLowConfidenceNutritionValidationSummary {

        val identicalCount =
            entries.count {
                it.decisionType ==
                        RepresentativeNutritionMappingDecisionType.IDENTICAL
            }

        val representativeCount =
            entries.count {
                it.decisionType ==
                        RepresentativeNutritionMappingDecisionType.REPRESENTATIVE
            }

        val incompatibleCount =
            entries.count {
                it.decisionType ==
                        RepresentativeNutritionMappingDecisionType.INCOMPATIBLE
            }

        val acceptedCount =
            entries.count {
                it.accepted
            }

        return RejectedLowConfidenceNutritionValidationSummary(
            rejectedLowConfidenceCount =
                entries.size,
            identicalCount =
                identicalCount,
            representativeCount =
                representativeCount,
            incompatibleCount =
                incompatibleCount,
            acceptedCount =
                acceptedCount,
            stillRejectedCount =
                entries.size - acceptedCount
        )
    }

    private fun readSelectedCandidateRanks(
        candidateQualityFile: File
    ): Map<CandidateIdentity, Int> {

        val root =
            JsonParser.parseString(
                candidateQualityFile.readText()
            )
                .asJsonObject

        val entries =
            root.getAsJsonArray("entries")
                ?: error(
                    "Missing entries array in rejected nutrition " +
                            "candidate quality report: " +
                            candidateQualityFile.absolutePath
                )

        val result =
            linkedMapOf<CandidateIdentity, Int>()

        entries.forEach { entryElement ->

            val entry =
                entryElement.asJsonObject

            val validationStatus =
                entry.nullableString(
                    key =
                        "validationStatus"
                )

            if (
                validationStatus != null &&
                validationStatus != REJECTED_LOW_CONFIDENCE
            ) {
                return@forEach
            }

            val catalogKey =
                entry.requiredString(
                    key =
                        "catalogKey",
                    source =
                        "rejected nutrition candidate quality entry"
                )

            val selectedCandidateRank =
                entry.optionalInt(
                    key =
                        "selectedCandidateRank"
                )
                    ?: return@forEach

            require(selectedCandidateRank > 0) {
                "Selected candidate rank must be greater than " +
                        "zero for: $catalogKey"
            }

            val candidates =
                entry.getAsJsonArray("candidates")
                    ?: error(
                        "Missing candidates array for rejected " +
                                "nutrition candidate quality entry: " +
                                catalogKey
                    )

            require(
                selectedCandidateRank <= candidates.size()
            ) {
                "Selected candidate rank exceeds candidate count " +
                        "for '$catalogKey': " +
                        "rank=$selectedCandidateRank, " +
                        "candidates=${candidates.size()}"
            }

            val selectedCandidates =
                candidates
                    .mapIndexedNotNull { index, candidateElement ->

                        val candidate =
                            candidateElement.asJsonObject

                        val selected =
                            candidate.optionalBoolean(
                                key =
                                    "selected"
                            )
                                ?: false

                        if (!selected) {
                            return@mapIndexedNotNull null
                        }

                        SelectedCandidate(
                            rank =
                                index + 1,
                            serverKey =
                                candidate.requiredString(
                                    key =
                                        "serverKey",
                                    source =
                                        "rejected nutrition candidate"
                                )
                        )
                    }

            require(selectedCandidates.size == 1) {
                "Expected exactly one selected candidate for " +
                        "'$catalogKey', but found " +
                        selectedCandidates.size
            }

            val selectedCandidate =
                selectedCandidates.single()

            require(
                selectedCandidate.rank ==
                        selectedCandidateRank
            ) {
                "Selected candidate rank mismatch for " +
                        "'$catalogKey': report rank=" +
                        "$selectedCandidateRank, selected candidate " +
                        "rank=${selectedCandidate.rank}"
            }

            val identity =
                CandidateIdentity(
                    catalogKey =
                        catalogKey,
                    serverKey =
                        selectedCandidate.serverKey
                )

            require(identity !in result) {
                "Duplicate selected rejected nutrition candidate: " +
                        "$catalogKey -> ${selectedCandidate.serverKey}"
            }

            result[identity] =
                selectedCandidateRank
        }

        return result
    }

    private fun readDiagnostics(
        diagnosticsFile: File
    ): List<MatchDiagnostic> {

        val root =
            JsonParser.parseString(
                diagnosticsFile.readText()
            )
                .asJsonObject

        val diagnostics =
            root.getAsJsonArray("diagnostics")
                ?: error(
                    "Missing diagnostics array in: " +
                            diagnosticsFile.absolutePath
                )

        return diagnostics.map { element ->

            val json =
                element.asJsonObject

            MatchDiagnostic(
                catalogKey =
                    json.requiredString(
                        key =
                            "catalogKey",
                        source =
                            "nutrition match diagnostic"
                    ),
                selectedServerKey =
                    json.nullableString(
                        key =
                            "selectedServerKey"
                    ),
                confidence =
                    json.requiredDouble(
                        key =
                            "confidence",
                        source =
                            "nutrition match diagnostic"
                    ),
                decisionReason =
                    json.nullableString(
                        key =
                            "decisionReason"
                    ),
                validationStatus =
                    json.requiredString(
                        key =
                            "validationStatus",
                        source =
                            "nutrition match diagnostic"
                    ),
                validationReason =
                    json.nullableString(
                        key =
                            "validationReason"
                    )
            )
        }
    }

    private fun writeReport(
        report:
        RejectedLowConfidenceNutritionValidationReport,
        outputFile: File
    ) {
        outputFile.parentFile
            ?.let { directory ->

                if (!directory.exists()) {
                    check(directory.mkdirs()) {
                        "Could not create report directory: " +
                                directory.absolutePath
                    }
                }

                require(directory.isDirectory) {
                    "Report parent path is not a directory: " +
                            directory.absolutePath
                }
            }

        val gson =
            GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()

        outputFile.writeText(
            gson.toJson(report) + "\n"
        )
    }

    private fun printReport(
        report:
        RejectedLowConfidenceNutritionValidationReport,
        outputFile: File,
        output: PrintStream
    ) {
        val summary =
            report.summary

        output.println()
        output.println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
        output.println(
            "REJECTED LOW-CONFIDENCE NUTRITION VALIDATION"
        )
        output.println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
        output.println(
            "Rejected low-confidence : " +
                    summary.rejectedLowConfidenceCount
        )
        output.println()
        output.println(
            "IDENTICAL                : " +
                    summary.identicalCount
        )
        output.println(
            "REPRESENTATIVE           : " +
                    summary.representativeCount
        )
        output.println(
            "INCOMPATIBLE             : " +
                    summary.incompatibleCount
        )
        output.println()
        output.println(
            "Accepted                  : " +
                    summary.acceptedCount
        )
        output.println(
            "Still rejected            : " +
                    summary.stillRejectedCount
        )
        output.println()
        output.println(
            "Output                    : " +
                    outputFile.absolutePath
        )
        output.println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
    }

    private fun JsonObject.requiredString(
        key: String,
        source: String
    ): String {

        return nullableString(
            key =
                key
        )
            ?: error(
                "Missing or blank string '$key' in $source."
            )
    }

    private fun JsonObject.nullableString(
        key: String
    ): String? {

        val element =
            get(key)
                ?: return null

        if (
            element.isJsonNull ||
            !element.isJsonPrimitive
        ) {
            return null
        }

        return element
            .asString
            .trim()
            .takeIf {
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

    private data class SelectedCandidate(
        val rank: Int,
        val serverKey: String
    )

    private data class CandidateIdentity(
        val catalogKey: String,
        val serverKey: String
    )

    private data class MatchDiagnostic(
        val catalogKey: String,
        val selectedServerKey: String?,
        val confidence: Double,
        val decisionReason: String?,
        val validationStatus: String,
        val validationReason: String?
    )

    private companion object {

        const val REJECTED_LOW_CONFIDENCE =
            "REJECTED_LOW_CONFIDENCE"
    }
}