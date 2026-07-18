package de.shopme.tools.knowledge.rebuild.nutrition.coverage

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.util.Locale

class NutritionCatalogKeyJsonIndex {

    fun readCatalogKeys(
        file: File
    ): Set<String> =
        readEntries(
            file = file,
            acceptedKeyFields = CATALOG_KEY_FIELDS
        )
            .mapTo(linkedSetOf()) {
                it.catalogKey
            }

    fun readRequestKeys(
        file: File
    ): Set<String> =
        readEntries(
            file = file,
            acceptedKeyFields = REQUEST_KEY_FIELDS
        )
            .mapTo(linkedSetOf()) {
                it.catalogKey
            }

    fun readMappingKeys(
        file: File,
        requiredArtifactName: String
    ): Set<String> =
        readEntries(
            file = file,
            acceptedKeyFields = MAPPING_KEY_FIELDS
        )
            .filter { entry ->
                val artifact =
                    entry.objectValue
                        ?.optionalString(
                            "serverArtifact"
                        )
                        ?: entry.objectValue
                            ?.optionalString(
                                "artifactName"
                            )

                artifact == null ||
                        artifact.equals(
                            requiredArtifactName,
                            ignoreCase = true
                        )
            }
            .mapTo(linkedSetOf()) {
                it.catalogKey
            }

    fun readMatchReportEntries(
        directory: File,
        artifactName: String
    ): Map<String, MatchReportIndexEntry> {

        require(directory.isDirectory) {
            "Match-report directory does not exist: ${directory.absolutePath}"
        }

        val matchingFiles =
            directory
                .walkTopDown()
                .filter {
                    it.isFile &&
                            it.extension.equals(
                                "json",
                                ignoreCase = true
                            ) &&
                            it.name.contains(
                                artifactName.substringBeforeLast(".json"),
                                ignoreCase = true
                            )
                }
                .sortedBy {
                    it.absolutePath
                }
                .toList()

        val filesToRead =
            if (matchingFiles.isNotEmpty()) {
                matchingFiles
            } else {
                directory
                    .walkTopDown()
                    .filter {
                        it.isFile &&
                                it.extension.equals(
                                    "json",
                                    ignoreCase = true
                                )
                    }
                    .sortedBy {
                        it.absolutePath
                    }
                    .toList()
            }

        val result =
            linkedMapOf<String, MatchReportIndexEntry>()

        filesToRead.forEach { file ->

            readEntries(
                file = file,
                acceptedKeyFields = MATCH_REPORT_KEY_FIELDS
            )
                .forEach { indexed ->

                    val objectValue =
                        indexed.objectValue

                    val status =
                        objectValue
                            ?.firstString(
                                "status",
                                "matchStatus",
                                "result",
                                "decisionType"
                            )
                            ?.uppercase(Locale.ROOT)

                    val selectedServerKey =
                        objectValue
                            ?.firstString(
                                "serverKey",
                                "selectedServerKey",
                                "matchedServerKey",
                                "targetKey"
                            )

                    val nearestCandidateCount =
                        objectValue
                            ?.get("nearestCandidates")
                            ?.takeIf {
                                it.isJsonArray
                            }
                            ?.asJsonArray
                            ?.size()

                    val explicitlyMatched =
                        objectValue
                            ?.firstBoolean(
                                "matched",
                                "isMatched",
                                "matchExists"
                            )

                    val explicitlyUnmatched =
                        objectValue
                            ?.firstBoolean(
                                "unmatched",
                                "isUnmatched"
                            )

                    val matched =
                        explicitlyMatched == true ||
                                selectedServerKey != null ||
                                status in MATCHED_STATUSES

                    val unmatched =
                        explicitlyUnmatched == true ||
                                status in UNMATCHED_STATUSES ||
                                (
                                        explicitlyMatched == false &&
                                                selectedServerKey == null
                                        )

                    val existing =
                        result[indexed.catalogKey]

                    val merged =
                        MatchReportIndexEntry(
                            catalogKey =
                                indexed.catalogKey,
                            present =
                                true,
                            matched =
                                matched ||
                                        existing?.matched == true,
                            unmatched =
                                unmatched ||
                                        existing?.unmatched == true,
                            nearestCandidateCount =
                                mergeCandidateCounts(
                                    current =
                                        nearestCandidateCount,
                                    existing =
                                        existing?.nearestCandidateCount
                                )
                        )

                    result[indexed.catalogKey] =
                        merged
                }
        }

        return result
            .toSortedMap()
    }

    private fun readEntries(
        file: File,
        acceptedKeyFields: Set<String>
    ): List<IndexedJsonEntry> {

        require(file.isFile) {
            "JSON file does not exist: ${file.absolutePath}"
        }

        val root =
            JsonParser.parseString(
                file.readText()
            )

        val entries =
            mutableListOf<IndexedJsonEntry>()

        visit(
            value = root,
            parentPropertyName = null,
            acceptedKeyFields = acceptedKeyFields,
            output = entries
        )

        return entries
            .distinctBy {
                it.catalogKey to it.objectValue?.toString()
            }
            .sortedBy {
                it.catalogKey
            }
    }

    private fun visit(
        value: JsonElement,
        parentPropertyName: String?,
        acceptedKeyFields: Set<String>,
        output: MutableList<IndexedJsonEntry>
    ) {
        when {
            value.isJsonObject -> {
                val objectValue =
                    value.asJsonObject

                val explicitKey =
                    acceptedKeyFields
                        .asSequence()
                        .mapNotNull { field ->
                            objectValue.optionalString(
                                field
                            )
                        }
                        .firstOrNull()

                if (explicitKey != null) {
                    output +=
                        IndexedJsonEntry(
                            catalogKey =
                                normalizeKey(
                                    explicitKey
                                ),
                            objectValue =
                                objectValue
                        )
                } else if (
                    parentPropertyName != null &&
                    looksLikeCatalogKey(
                        parentPropertyName
                    ) &&
                    looksLikeEntryObject(
                        objectValue
                    )
                ) {
                    output +=
                        IndexedJsonEntry(
                            catalogKey =
                                normalizeKey(
                                    parentPropertyName
                                ),
                            objectValue =
                                objectValue
                        )
                }

                objectValue
                    .entrySet()
                    .sortedBy {
                        it.key
                    }
                    .forEach { (
                                   propertyName,
                                   child
                               ) ->

                        visit(
                            value =
                                child,
                            parentPropertyName =
                                propertyName,
                            acceptedKeyFields =
                                acceptedKeyFields,
                            output =
                                output
                        )
                    }
            }

            value.isJsonArray -> {
                value
                    .asJsonArray
                    .forEach { child ->
                        visit(
                            value =
                                child,
                            parentPropertyName =
                                null,
                            acceptedKeyFields =
                                acceptedKeyFields,
                            output =
                                output
                        )
                    }
            }
        }
    }

    private fun looksLikeCatalogKey(
        value: String
    ): Boolean {
        val normalized =
            normalizeKey(
                value
            )

        return normalized.isNotBlank() &&
                normalized !in STRUCTURAL_PROPERTY_NAMES &&
                normalized.any {
                    it.isLetter()
                }
    }

    private fun looksLikeEntryObject(
        value: JsonObject
    ): Boolean =
        value.entrySet().isNotEmpty() &&
                value.entrySet().any { entry ->
                    entry.value.isJsonPrimitive ||
                            entry.value.isJsonArray ||
                            entry.key in ENTRY_HINT_FIELDS
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

    private fun JsonObject.firstString(
        vararg keys: String
    ): String? =
        keys
            .asSequence()
            .mapNotNull {
                optionalString(
                    it
                )
            }
            .firstOrNull()

    private fun JsonObject.firstBoolean(
        vararg keys: String
    ): Boolean? =
        keys
            .asSequence()
            .mapNotNull { key ->
                get(key)
                    ?.takeIf {
                        !it.isJsonNull &&
                                it.isJsonPrimitive &&
                                it.asJsonPrimitive.isBoolean
                    }
                    ?.asBoolean
            }
            .firstOrNull()

    data class MatchReportIndexEntry(
        val catalogKey: String,
        val present: Boolean,
        val matched: Boolean,
        val unmatched: Boolean,
        val nearestCandidateCount: Int?
    )

    private data class IndexedJsonEntry(
        val catalogKey: String,
        val objectValue: JsonObject?
    )

    private fun mergeCandidateCounts(
        current: Int?,
        existing: Int?
    ): Int? =
        when {
            current == null ->
                existing

            existing == null ->
                current

            else ->
                maxOf(
                    current,
                    existing
                )
        }

    private companion object {

        val WHITESPACE_REGEX =
            Regex("\\s+")

        val CATALOG_KEY_FIELDS =
            setOf(
                "catalogKey",
                "canonicalKey",
                "foodKey",
                "key",
                "id"
            )

        val REQUEST_KEY_FIELDS =
            setOf(
                "catalogKey",
                "canonicalKey",
                "foodKey"
            )

        val MAPPING_KEY_FIELDS =
            setOf(
                "catalogKey",
                "canonicalKey",
                "foodKey"
            )

        val MATCH_REPORT_KEY_FIELDS =
            setOf(
                "catalogKey",
                "canonicalKey",
                "foodKey",
                "unmatchedCatalogKey"
            )

        val STRUCTURAL_PROPERTY_NAMES =
            setOf(
                "version",
                "entries",
                "items",
                "foods",
                "requests",
                "mappings",
                "matches",
                "unmatched",
                "matched",
                "metadata",
                "statistics",
                "summary",
                "groups",
                "gaps"
            )

        val ENTRY_HINT_FIELDS =
            setOf(
                "name",
                "catalogKey",
                "canonicalKey",
                "serverKey",
                "selectedServerKey",
                "candidates",
                "status",
                "matched",
                "unmatched"
            )

        val MATCHED_STATUSES =
            setOf(
                "MATCH",
                "MATCHED",
                "ACCEPTED",
                "EXACT_MATCH",
                "DETERMINISTIC_MATCH"
            )

        val UNMATCHED_STATUSES =
            setOf(
                "NO_MATCH",
                "UNMATCHED",
                "REJECTED",
                "MISSING"
            )
    }
}