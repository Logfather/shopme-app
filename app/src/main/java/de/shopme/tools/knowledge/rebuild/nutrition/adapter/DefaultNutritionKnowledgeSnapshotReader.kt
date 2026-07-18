package de.shopme.tools.knowledge.rebuild.nutrition.adapter

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeRebuildSnapshot
import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeSnapshotReader
import java.io.File

class DefaultNutritionKnowledgeSnapshotReader(
    private val catalogFile: File,
    private val exactMappingFile: File,
    private val runtimeNutritionFile: File,
    private val mappingFile: File
) : NutritionKnowledgeSnapshotReader {

    override fun read():
            NutritionKnowledgeRebuildSnapshot {

        val catalogKeys =
            readCatalogKeys(
                file =
                    catalogFile
            )

        val exactMappings =
            readMappings(
                file =
                    exactMappingFile,
                sourceName =
                    "exact nutrition mapping file",
                requireFile =
                    true
            )

        val catalogServerMappings =
            readMappings(
                file =
                    mappingFile,
                sourceName =
                    "catalog-server mapping file",
                requireFile =
                    false
            )

        /*
         * nutrition.mappings.json ist die deterministisch erzeugte
         * Quelle für Exact Matches.
         *
         * Es wird nicht mehr versucht, das große Server-Artefakt
         * heuristisch zu interpretieren.
         */
        val exactCatalogKeys =
            exactMappings
                .keys
                .intersect(
                    catalogKeys
                )
                .toSortedSet()

        /*
         * Ein Catalog Key, der bereits exakt gemappt ist, darf nicht
         * zusätzlich als AI-/Representative-Mapping gezählt werden.
         */
        val mappedCatalogKeys =
            catalogServerMappings
                .keys
                .intersect(
                    catalogKeys
                )
                .minus(
                    exactCatalogKeys
                )
                .toSortedSet()

        val coveredCatalogKeys =
            (
                    exactCatalogKeys +
                            mappedCatalogKeys
                    )
                .toSortedSet()

        val runtimeEntryCount =
            if (runtimeNutritionFile.isFile) {

                countKnowledgeEntries(
                    file =
                        runtimeNutritionFile,
                    sourceName =
                        "runtime nutrition artifact"
                )

            } else {

                coveredCatalogKeys.size
            }

        val catalogItemCount =
            catalogKeys.size

        val exactMatchCount =
            exactCatalogKeys.size

        val mappedMatchCount =
            mappedCatalogKeys.size

        val coveredCount =
            coveredCatalogKeys.size

        val missingCount =
            catalogItemCount -
                    coveredCount

        /*
         * Das Runtime-Artefakt ist die finale materielle Kontrolle.
         * Seine Eintragszahl muss der disjunkten Summe aus Exact und
         * Catalog→Server-Mappings entsprechen.
         */
        require(
            runtimeEntryCount ==
                    coveredCount
        ) {
            "Runtime nutrition entry count differs from calculated " +
                    "coverage: runtime=$runtimeEntryCount, " +
                    "exact=$exactMatchCount, " +
                    "mapped=$mappedMatchCount, " +
                    "covered=$coveredCount."
        }

        return NutritionKnowledgeRebuildSnapshot(
            mappingCount =
                catalogServerMappings.size,
            catalogItemCount =
                catalogItemCount,
            exactMatchCount =
                exactMatchCount,
            mappedMatchCount =
                mappedMatchCount,
            runtimeEntryCount =
                runtimeEntryCount,
            coveredCatalogItemCount =
                coveredCount,
            missingCatalogItemCount =
                missingCount,
            coverage =
                if (catalogItemCount == 0) {
                    0.0
                } else {
                    coveredCount.toDouble() /
                            catalogItemCount.toDouble()
                }
        )
    }

    private fun readCatalogKeys(
        file: File
    ): Set<String> {

        require(file.isFile) {
            "Catalog file does not exist: " +
                    file.absolutePath
        }

        val root =
            JsonParser.parseString(
                file.readText()
            )

        val items =
            when {

                root.isJsonArray ->
                    root.asJsonArray

                root.isJsonObject -> {

                    val rootObject =
                        root.asJsonObject

                    rootObject.arrayOrNull(
                        key =
                            "items"
                    )
                        ?: rootObject.arrayOrNull(
                            key =
                                "foods"
                        )
                        ?: rootObject.arrayOrNull(
                            key =
                                "products"
                        )
                        ?: error(
                            "Catalog contains no 'items', 'foods' " +
                                    "or 'products' array: " +
                                    file.absolutePath
                        )
                }

                else ->
                    error(
                        "Unsupported catalog JSON root in: " +
                                file.absolutePath
                    )
            }

        val normalizedKeys =
            items
                .mapNotNull { element ->

                    if (!element.isJsonObject) {
                        return@mapNotNull null
                    }

                    val item =
                        element.asJsonObject

                    item.optionalString(
                        key =
                            "normalizedEnglish"
                    )
                        ?: item.optionalString(
                            key =
                                "normalized"
                        )
                        ?: item.optionalString(
                            key =
                                "canonicalKey"
                        )
                        ?: item.optionalString(
                            key =
                                "canonicalId"
                        )
                        ?: item.optionalString(
                            key =
                                "id"
                        )
                        ?: item.optionalString(
                            key =
                                "name"
                        )
                }
                .map(
                    ::normalizeKey
                )
                .filter {
                    it.isNotBlank()
                }
                .toSortedSet()

        require(normalizedKeys.isNotEmpty()) {
            "Catalog contains no readable normalized keys: " +
                    file.absolutePath
        }

        /*
         * Mehrere Catalog-Zeilen dürfen denselben normalisierten
         * Knowledge-Key besitzen. Für die Runtime-Coverage zählt
         * jeder eindeutige Key genau einmal.
         */
        return normalizedKeys
    }

    private fun readMappings(
        file: File,
        sourceName: String,
        requireFile: Boolean
    ): Map<String, String> {

        if (!file.isFile) {

            require(!requireFile) {
                "$sourceName does not exist: " +
                        file.absolutePath
            }

            return emptyMap()
        }

        val root =
            JsonParser.parseString(
                file.readText()
            )

        require(root.isJsonObject) {
            "$sourceName must contain a JSON object: " +
                    file.absolutePath
        }

        val mappings =
            root.asJsonObject
                .arrayOrNull(
                    key =
                        "mappings"
                )
                ?: error(
                    "$sourceName contains no 'mappings' array: " +
                            file.absolutePath
                )

        val entries =
            mappings.mapNotNull { element ->

                require(element.isJsonObject) {
                    "$sourceName mapping entry must be a JSON object."
                }

                val mapping =
                    element.asJsonObject

                val serverArtifact =
                    mapping.optionalString(
                        key =
                            "serverArtifact"
                    )
                        ?: NUTRITION_ARTIFACT

                if (
                    serverArtifact !=
                    NUTRITION_ARTIFACT
                ) {
                    return@mapNotNull null
                }

                val catalogKey =
                    normalizeKey(
                        mapping.requiredString(
                            key =
                                "catalogKey"
                        )
                    )

                val serverKey =
                    normalizeKey(
                        mapping.requiredString(
                            key =
                                "serverKey"
                        )
                    )

                catalogKey to
                        serverKey
            }

        val conflictingCatalogKeys =
            entries
                .groupBy {
                    it.first
                }
                .filterValues { values ->

                    values
                        .map {
                            it.second
                        }
                        .distinct()
                        .size >
                            1
                }
                .keys

        require(conflictingCatalogKeys.isEmpty()) {
            "$sourceName contains conflicting mappings for " +
                    "catalog keys: " +
                    conflictingCatalogKeys
                        .sorted()
                        .take(MAX_DIAGNOSTIC_KEYS)
                        .joinToString()
        }

        return entries
            .associate {
                it.first to
                        it.second
            }
            .toSortedMap()
    }

    private fun countKnowledgeEntries(
        file: File,
        sourceName: String
    ): Int {

        require(file.isFile) {
            "$sourceName does not exist: " +
                    file.absolutePath
        }

        val root =
            JsonParser.parseString(
                file.readText()
            )

        return countKnowledgeEntries(
            element =
                root,
            sourceName =
                sourceName,
            sourcePath =
                file.absolutePath
        )
    }

    private fun countKnowledgeEntries(
        element: JsonElement,
        sourceName: String,
        sourcePath: String
    ): Int {

        return when {

            element.isJsonArray -> {

                element.asJsonArray.size()
            }

            element.isJsonObject -> {

                val objectValue =
                    element.asJsonObject

                val container =
                    findKnowledgeContainer(
                        root =
                            objectValue
                    )

                if (container != null) {

                    countKnowledgeEntries(
                        element =
                            container,
                        sourceName =
                            sourceName,
                        sourcePath =
                            sourcePath
                    )

                } else {

                    objectValue
                        .entrySet()
                        .count {
                            it.key !in
                                    METADATA_KEYS
                        }
                }
            }

            else ->
                error(
                    "Unsupported $sourceName JSON structure: " +
                            sourcePath
                )
        }
    }

    private fun findKnowledgeContainer(
        root: JsonObject
    ): JsonElement? {

        return KNOWLEDGE_CONTAINER_KEYS
            .asSequence()
            .mapNotNull { key ->

                root.get(key)
                    ?.takeIf { value ->

                        !value.isJsonNull &&
                                (
                                        value.isJsonArray ||
                                                value.isJsonObject
                                        )
                    }
            }
            .firstOrNull()
    }

    private fun JsonObject.arrayOrNull(
        key: String
    ): JsonArray? {

        return get(key)
            ?.takeIf {
                it.isJsonArray
            }
            ?.asJsonArray
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

    private fun normalizeKey(
        value: String
    ): String {

        return value
            .trim()
            .lowercase()
            .replace(
                "-",
                " "
            )
            .replace(
                "_",
                " "
            )
            .replace(
                WHITESPACE_REGEX,
                " "
            )
            .trim()
    }

    private companion object {

        const val NUTRITION_ARTIFACT =
            "nutrition.json"

        const val MAX_DIAGNOSTIC_KEYS =
            10

        val WHITESPACE_REGEX =
            Regex("\\s+")

        val KNOWLEDGE_CONTAINER_KEYS =
            listOf(
                "entries",
                "items",
                "foods",
                "nutrition",
                "knowledge",
                "data",
                "values"
            )

        val METADATA_KEYS =
            setOf(
                "version",
                "artifact",
                "artifactType",
                "generatedAt",
                "generatedAtEpochMillis",
                "metadata",
                "summary",
                "schema",
                "source",
                "sources",
                "count",
                "entryCount"
            )
    }
}