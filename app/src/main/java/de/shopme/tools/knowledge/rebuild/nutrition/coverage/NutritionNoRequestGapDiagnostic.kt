package de.shopme.tools.knowledge.rebuild.nutrition.coverage

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.util.Locale

class NutritionNoRequestGapDiagnostic(
    private val jsonIndex:
    NutritionCatalogKeyJsonIndex =
        NutritionCatalogKeyJsonIndex()
) {

    fun diagnose(
        coverageGapReportFile: File,
        matchReportDirectory: File,
        matchRequestFile: File,
        mappingFile: File,
        serverArtifactName: String = "nutrition.json"
    ): NutritionNoRequestGapDiagnosticReport {

        val noRequestKeys =
            readNoRequestKeys(
                coverageGapReportFile =
                    coverageGapReportFile
            )

        val matchReportEntries =
            jsonIndex.readMatchReportEntries(
                directory =
                    matchReportDirectory,
                artifactName =
                    serverArtifactName
            )

        val requestKeys =
            jsonIndex.readRequestKeys(
                file =
                    matchRequestFile
            )

        val mappingKeys =
            jsonIndex.readMappingKeys(
                file =
                    mappingFile,
                requiredArtifactName =
                    serverArtifactName
            )

        val entries =
            noRequestKeys
                .sorted()
                .map { catalogKey ->

                    diagnoseEntry(
                        catalogKey =
                            catalogKey,
                        matchReportEntries =
                            matchReportEntries,
                        requestKeys =
                            requestKeys,
                        mappingKeys =
                            mappingKeys
                    )
                }

        val reasonCounts =
            entries
                .groupingBy {
                    it.reason.name
                }
                .eachCount()
                .toSortedMap()

        return NutritionNoRequestGapDiagnosticReport(
            version =
                REPORT_VERSION,
            noRequestGapCount =
                entries.size,
            reasonCounts =
                reasonCounts,
            entries =
                entries
        )
    }

    private fun diagnoseEntry(
        catalogKey: String,
        matchReportEntries:
        Map<String, NutritionCatalogKeyJsonIndex.MatchReportIndexEntry>,
        requestKeys: Set<String>,
        mappingKeys: Set<String>
    ): NutritionNoRequestGapDiagnosticEntry {

        val normalizedCatalogKey =
            normalizeKey(
                catalogKey
            )

        /*
         * Every key passed into this method originates from
         * nutrition.coverage-gaps.json. The coverage report itself is generated
         * from the canonical catalog, so catalog presence is already guaranteed.
         */
        val catalogPresent =
            true

        val matchReportEntry =
            matchReportEntries[
                normalizedCatalogKey
            ]

        val matchReportEntryPresent =
            matchReportEntry != null

        val matchReportEntryMatched =
            matchReportEntry?.matched == true

        val matchReportEntryUnmatched =
            matchReportEntry?.unmatched == true

        val nearestCandidateCount =
            matchReportEntry?.nearestCandidateCount

        val requestPresent =
            normalizedCatalogKey in requestKeys

        val mappingPresent =
            normalizedCatalogKey in mappingKeys

        val reason =
            when {
                mappingPresent ->
                    NutritionNoRequestGapReason
                        .MAPPING_PRESENT_BUT_COVERAGE_REPORT_STALE

                requestPresent ->
                    NutritionNoRequestGapReason
                        .REQUEST_PRESENT_BUT_COVERAGE_REPORT_STALE

                !matchReportEntryPresent ->
                    NutritionNoRequestGapReason
                        .MATCH_REPORT_ENTRY_MISSING

                matchReportEntryMatched ->
                    NutritionNoRequestGapReason
                        .MATCH_REPORT_ALREADY_MATCHED

                nearestCandidateCount == 0 ->
                    NutritionNoRequestGapReason
                        .NO_CANDIDATES

                matchReportEntryUnmatched ->
                    NutritionNoRequestGapReason
                        .UNMATCHED_ENTRY_NOT_CONVERTED_TO_REQUEST

                else ->
                    NutritionNoRequestGapReason
                        .MATCH_REPORT_ENTRY_WITHOUT_MATCH_STATUS
            }

        val details =
            when (reason) {

                NutritionNoRequestGapReason.NO_CANDIDATES ->
                    "The catalog key is present in the nutrition catalog-server " +
                            "match report, but nearestCandidates is empty. No match " +
                            "request can be generated because candidate retrieval " +
                            "produced no selectable server knowledge candidate."

                NutritionNoRequestGapReason
                    .MATCH_REPORT_ENTRY_WITHOUT_MATCH_STATUS ->
                    "The catalog key occurs in the nutrition catalog-server match " +
                            "report, but the entry is classified as neither matched nor " +
                            "unmatched. The request generator therefore has no explicit " +
                            "unmatched state from which to create a request."

                NutritionNoRequestGapReason.MATCH_REPORT_ENTRY_MISSING ->
                    "The catalog key is present in the nutrition coverage-gap " +
                            "report but does not occur in the generated nutrition " +
                            "catalog-server match reports."

                NutritionNoRequestGapReason.MATCH_REPORT_ALREADY_MATCHED ->
                    "The catalog key is represented as matched in the " +
                            "catalog-server match report, but no runtime nutrition " +
                            "coverage exists."

                NutritionNoRequestGapReason
                    .UNMATCHED_ENTRY_NOT_CONVERTED_TO_REQUEST ->
                    "The catalog key reached the catalog-server match report " +
                            "as an unmatched item, but no persisted nutrition " +
                            "match request was generated."

                NutritionNoRequestGapReason
                    .REQUEST_PRESENT_BUT_COVERAGE_REPORT_STALE ->
                    "A persisted nutrition match request exists although the " +
                            "coverage-gap report classifies the catalog key as " +
                            "NO_REQUEST."

                NutritionNoRequestGapReason
                    .MAPPING_PRESENT_BUT_COVERAGE_REPORT_STALE ->
                    "A central nutrition catalog-server mapping exists although " +
                            "the coverage-gap report classifies the catalog key as " +
                            "NO_REQUEST."
            }

        return NutritionNoRequestGapDiagnosticEntry(
            catalogKey =
                normalizedCatalogKey,
            reason =
                reason,
            catalogPresent =
                catalogPresent,
            matchReportEntryPresent =
                matchReportEntryPresent,
            matchReportEntryMatched =
                matchReportEntryMatched,
            matchReportEntryUnmatched =
                matchReportEntryUnmatched,
            nearestCandidateCount =
                nearestCandidateCount,
            requestPresent =
                requestPresent,
            mappingPresent =
                mappingPresent,
            details =
                details
        )
    }

    private fun readNoRequestKeys(
        coverageGapReportFile: File
    ): Set<String> {

        require(coverageGapReportFile.isFile) {
            "Nutrition coverage-gap report does not exist: " +
                    coverageGapReportFile.absolutePath
        }

        val root =
            JsonParser.parseString(
                coverageGapReportFile.readText()
            )

        require(root.isJsonObject) {
            "Nutrition coverage-gap report must be a JSON object."
        }

        val objectValue =
            root.asJsonObject

        val gaps =
            objectValue
                .get("gaps")
                ?.takeIf {
                    it.isJsonArray
                }
                ?.asJsonArray
                ?: error(
                    "Nutrition coverage-gap report contains no gaps array."
                )

        return gaps
            .mapNotNull { element ->

                if (!element.isJsonObject) {
                    return@mapNotNull null
                }

                val gap =
                    element.asJsonObject

                val type =
                    gap.optionalString(
                        key =
                            "type"
                    )

                val requestExists =
                    gap.optionalBoolean(
                        key =
                            "requestExists"
                    )

                if (
                    type != NO_REQUEST_TYPE &&
                    requestExists != false
                ) {
                    return@mapNotNull null
                }

                gap.optionalString(
                    key =
                        "catalogKey"
                )
                    ?.let {
                        normalizeKey(
                            it
                        )
                    }
            }
            .toSortedSet()
    }

    private fun normalizeKey(
        value: String
    ): String =
        value
            .trim()
            .lowercase(Locale.ROOT)
            .replace(
                WHITESPACE_REGEX,
                " "
            )

    private fun JsonObject.optionalString(
        key: String
    ): String? =
        get(key)
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

    private fun JsonObject.optionalBoolean(
        key: String
    ): Boolean? =
        get(key)
            ?.takeIf {
                !it.isJsonNull &&
                        it.isJsonPrimitive &&
                        it.asJsonPrimitive.isBoolean
            }
            ?.asBoolean

    private companion object {

        const val REPORT_VERSION =
            1

        const val NO_REQUEST_TYPE =
            "NO_REQUEST"

        val WHITESPACE_REGEX =
            Regex("\\s+")
    }
}