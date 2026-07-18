package de.shopme.tools.knowledge.rebuild.nutrition.adapter

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class RepresentativeNutritionMappingMerger {

    fun merge(
        representativeValidationFile: File,
        mappingFile: File
    ): RepresentativeNutritionMappingMergeResult {

        require(representativeValidationFile.isFile) {
            "Representative nutrition validation file does not exist: " +
                    representativeValidationFile.absolutePath
        }

        val existingMappings =
            readMappings(
                file =
                    mappingFile
            )

        val representativeMappings =
            readAcceptedRepresentativeMappings(
                file =
                    representativeValidationFile
            )

        val mergedByCatalogKey =
            existingMappings
                .associateBy {
                    it.catalogKey
                }
                .toMutableMap()

        var addedCount =
            0

        var unchangedCount =
            0

        representativeMappings.forEach { representative ->

            val existing =
                mergedByCatalogKey[
                    representative.catalogKey
                ]

            when {

                existing == null -> {

                    mergedByCatalogKey[
                        representative.catalogKey
                    ] = representative

                    addedCount++
                }

                existing.serverArtifact ==
                        representative.serverArtifact &&
                        existing.serverKey ==
                        representative.serverKey -> {

                    unchangedCount++
                }

                else -> {

                    error(
                        "Representative nutrition mapping conflicts " +
                                "with an existing mapping: " +
                                "catalogKey='${representative.catalogKey}', " +
                                "existing='${existing.serverArtifact} -> " +
                                "${existing.serverKey}', " +
                                "representative='" +
                                "${representative.serverArtifact} -> " +
                                "${representative.serverKey}'."
                    )
                }
            }
        }

        val mergedMappings =
            mergedByCatalogKey
                .values
                .sortedWith(
                    MAPPING_ORDER
                )

        /*
         * Echte Idempotenz:
         *
         * Wenn sämtliche Representative-Mappings bereits identisch
         * vorhanden sind, wird die Mapping-Datei nicht erneut
         * serialisiert. Dadurch bleiben Inhalt, Formatierung,
         * Dateizeitpunkt und Bytes unverändert.
         */

        require(
            mergedMappings.size ==
                    existingMappings.size +
                    addedCount
        ) {
            "Merged mapping count is inconsistent: " +
                    "existing=${existingMappings.size}, " +
                    "added=$addedCount, " +
                    "merged=${mergedMappings.size}."
        }

        if (addedCount > 0) {

            persistMappings(
                mappings =
                    mergedMappings,
                file =
                    mappingFile
            )
        }

        return RepresentativeNutritionMappingMergeResult(
            existingMappingCount =
                existingMappings.size,
            representativeMappingCount =
                representativeMappings.size,
            representativeAddedCount =
                addedCount,
            representativeUnchangedCount =
                unchangedCount,
            finalMappingCount =
                mergedMappings.size
        )
    }

    private fun readAcceptedRepresentativeMappings(
        file: File
    ): List<NutritionCatalogServerMapping> {

        val root =
            parseObject(
                file = file
            )

        val entries =
            root.requiredArray(
                key = "entries"
            )

        val mappings =
            entries.mapNotNull { element ->

                require(element.isJsonObject) {
                    "Representative nutrition validation entry " +
                            "must be a JSON object."
                }

                val entry =
                    element.asJsonObject

                val accepted =
                    entry.requiredBoolean(
                        key = "accepted"
                    )

                if (!accepted) {
                    return@mapNotNull null
                }

                val decisionType =
                    entry.requiredString(
                        key = "decisionType"
                    )

                require(
                    decisionType in
                            ACCEPTED_DECISION_TYPES
                ) {
                    "Accepted representative nutrition entry has " +
                            "unsupported decisionType '$decisionType': " +
                            entry.requiredString(
                                key = "catalogKey"
                            )
                }

                NutritionCatalogServerMapping(
                    catalogKey =
                        normalizeKey(
                            entry.requiredString(
                                key = "catalogKey"
                            )
                        ),
                    serverArtifact =
                        entry.optionalString(
                            key = "serverArtifact"
                        )
                            ?: NUTRITION_ARTIFACT,
                    serverKey =
                        normalizeKey(
                            entry.requiredString(
                                key = "selectedServerKey"
                            )
                        )
                )
            }

        validateMappings(
            mappings =
                mappings,
            sourceName =
                "representative nutrition validations"
        )

        return mappings
            .sortedWith(
                MAPPING_ORDER
            )
    }

    private fun readMappings(
        file: File
    ): List<NutritionCatalogServerMapping> {

        if (!file.isFile) {
            return emptyList()
        }

        val root =
            parseObject(
                file = file
            )

        val mappings =
            root.requiredArray(
                key = "mappings"
            )
                .mapNotNull { element ->

                    require(element.isJsonObject) {
                        "Catalog-server mapping entry must be a " +
                                "JSON object."
                    }

                    val mapping =
                        element.asJsonObject

                    val serverArtifact =
                        mapping.optionalString(
                            key = "serverArtifact"
                        )
                            ?: NUTRITION_ARTIFACT

                    /*
                     * Die zentrale Mapping-Datei kann zukünftig auch
                     * andere Dimensionen enthalten. Diese Einträge
                     * müssen erhalten bleiben.
                     */
                    NutritionCatalogServerMapping(
                        catalogKey =
                            normalizeKey(
                                mapping.requiredString(
                                    key = "catalogKey"
                                )
                            ),
                        serverArtifact =
                            serverArtifact,
                        serverKey =
                            normalizeKey(
                                mapping.requiredString(
                                    key = "serverKey"
                                )
                            )
                    )
                }

        validateMappings(
            mappings =
                mappings,
            sourceName =
                "catalog-server mapping file"
        )

        return mappings
            .sortedWith(
                MAPPING_ORDER
            )
    }

    private fun validateMappings(
        mappings:
        List<NutritionCatalogServerMapping>,
        sourceName: String
    ) {
        val conflicts =
            mappings
                .groupBy {
                    MappingIdentity(
                        catalogKey =
                            it.catalogKey,
                        serverArtifact =
                            it.serverArtifact
                    )
                }
                .filterValues { values ->

                    values
                        .map {
                            it.serverKey
                        }
                        .distinct()
                        .size >
                            1
                }

        require(conflicts.isEmpty()) {
            "$sourceName contains conflicting mappings: " +
                    conflicts.keys
                        .sortedWith(
                            compareBy<MappingIdentity>(
                                { it.serverArtifact },
                                { it.catalogKey }
                            )
                        )
                        .take(MAX_DIAGNOSTIC_MAPPINGS)
                        .joinToString {
                            "${it.catalogKey} -> ${it.serverArtifact}"
                        }
        }

        val duplicates =
            mappings
                .groupingBy {
                    MappingIdentity(
                        catalogKey =
                            it.catalogKey,
                        serverArtifact =
                            it.serverArtifact
                    )
                }
                .eachCount()
                .filterValues {
                    it > 1
                }

        require(duplicates.isEmpty()) {
            "$sourceName contains duplicate mappings: " +
                    duplicates.keys
                        .sortedWith(
                            compareBy<MappingIdentity>(
                                { it.serverArtifact },
                                { it.catalogKey }
                            )
                        )
                        .take(MAX_DIAGNOSTIC_MAPPINGS)
                        .joinToString {
                            "${it.catalogKey} -> ${it.serverArtifact}"
                        }
        }
    }

    private fun persistMappings(
        mappings:
        List<NutritionCatalogServerMapping>,
        file: File
    ) {
        val root =
            PersistedCatalogServerMappings(
                version =
                    CURRENT_VERSION,
                mappings =
                    mappings
            )

        writeJsonAtomically(
            value =
                root,
            file =
                file
        )
    }

    private fun writeJsonAtomically(
        value: Any,
        file: File
    ) {
        val directory =
            file.parentFile

        if (
            directory != null &&
            !directory.exists()
        ) {
            check(directory.mkdirs()) {
                "Could not create mapping directory: " +
                        directory.absolutePath
            }
        }

        val temporaryFile =
            File(
                directory,
                "${file.name}.tmp"
            )

        temporaryFile.writeText(
            GSON.toJson(value) + "\n"
        )

        try {
            Files.move(
                temporaryFile.toPath(),
                file.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            )

        } catch (
            exception: AtomicMoveNotSupportedException
        ) {
            Files.move(
                temporaryFile.toPath(),
                file.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
        }
    }

    private fun parseObject(
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
            key = key
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

    private fun JsonObject.requiredBoolean(
        key: String
    ): Boolean {

        val element =
            get(key)

        require(
            element != null &&
                    !element.isJsonNull &&
                    element.isJsonPrimitive &&
                    element.asJsonPrimitive.isBoolean
        ) {
            "Missing boolean '$key'."
        }

        return element.asBoolean
    }

    private fun normalizeKey(
        value: String
    ): String {

        return value
            .trim()
            .lowercase()
            .replace(
                WHITESPACE_REGEX,
                " "
            )
    }

    private data class MappingIdentity(
        val catalogKey: String,
        val serverArtifact: String
    )

    private data class PersistedCatalogServerMappings(
        val version: Int,
        val mappings:
        List<NutritionCatalogServerMapping>
    )

    companion object {

        const val CURRENT_VERSION =
            1

        const val NUTRITION_ARTIFACT =
            "nutrition.json"

        val ACCEPTED_DECISION_TYPES =
            setOf(
                "IDENTICAL",
                "REPRESENTATIVE"
            )

        val MAPPING_ORDER =
            compareBy<NutritionCatalogServerMapping>(
                { it.serverArtifact },
                { it.catalogKey },
                { it.serverKey }
            )

        private const val MAX_DIAGNOSTIC_MAPPINGS =
            10

        private val WHITESPACE_REGEX =
            Regex("\\s+")

        private val GSON: Gson =
            GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()
    }
}

data class NutritionCatalogServerMapping(
    val catalogKey: String,
    val serverArtifact: String,
    val serverKey: String
)

data class RepresentativeNutritionMappingMergeResult(
    val existingMappingCount: Int,
    val representativeMappingCount: Int,
    val representativeAddedCount: Int,
    val representativeUnchangedCount: Int,
    val finalMappingCount: Int
)