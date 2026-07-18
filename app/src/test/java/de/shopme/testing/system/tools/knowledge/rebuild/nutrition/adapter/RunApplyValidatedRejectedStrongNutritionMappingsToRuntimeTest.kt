package de.shopme.testing.system.tools.knowledge.rebuild.nutrition.adapter

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import de.shopme.tools.knowledge.rebuild.nutrition.adapter.DefaultNutritionKnowledgeSnapshotReader
import de.shopme.tools.knowledge.rebuild.nutrition.runner.NutritionKnowledgeRebuildProjectFiles
import de.shopme.tools.knowledge.runtime.CatalogRuntimeKnowledgeGenerator
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunApplyValidatedRejectedStrongNutritionMappingsToRuntimeTest {

    @Test
    fun applyValidatedRejectedStrongNutritionMappingsToRuntime() {

        val projectRoot =
            findProjectRoot()

        val files =
            NutritionKnowledgeRebuildProjectFiles
                .fromProjectRoot(
                    projectRoot =
                        projectRoot
                )

        requireInputFiles(
            files =
                files
        )

        val expectedMappings =
            EXPECTED_RUNTIME_MAPPINGS
                .associate {
                    normalizeKey(
                        value =
                            it.catalogKey
                    ) to
                            normalizeKey(
                                value =
                                    it.serverKey
                            )
                }
                .toSortedMap()

        val persistedMappingsBefore =
            readNutritionMappings(
                file =
                    files.outputMappingFile
            )

        expectedMappings
            .forEach { (
                           catalogKey,
                           expectedServerKey
                       ) ->

                assertEquals(
                    expected =
                        expectedServerKey,
                    actual =
                        persistedMappingsBefore[
                            catalogKey
                        ],
                    message =
                        "Required rejected-strong nutrition mapping " +
                                "is missing before runtime generation: " +
                                "'$catalogKey' -> '$expectedServerKey'."
                )
            }

        val runtimeEntriesBefore =
            readRuntimeEntries(
                file =
                    files.runtimeNutritionFile
            )

        CatalogRuntimeKnowledgeGenerator()
            .generate(
                catalogFile =
                    files.catalogFile,
                serverArtifactDirectory =
                    files.serverArtifactDirectory,
                runtimeArtifactDirectory =
                    files.runtimeArtifactDirectory,
                catalogServerMappingFile =
                    files.outputMappingFile
            )

        assertTrue(
            actual =
                files.runtimeNutritionFile.isFile,
            message =
                "Runtime nutrition artifact was not generated: " +
                        files.runtimeNutritionFile.absolutePath
        )

        val runtimeEntriesAfter =
            readRuntimeEntries(
                file =
                    files.runtimeNutritionFile
            )

        expectedMappings
            .forEach { (
                           catalogKey,
                           expectedServerKey
                       ) ->

                assertTrue(
                    actual =
                        catalogKey in
                                runtimeEntriesAfter,
                    message =
                        "Validated rejected-strong nutrition mapping " +
                                "was not applied to runtime: " +
                                "'$catalogKey' -> '$expectedServerKey'."
                )
            }

        val newlyMaterializedCatalogKeys =
            expectedMappings
                .keys
                .filter {
                    it !in
                            runtimeEntriesBefore
                }
                .sorted()

        assertEquals(
            expected =
                runtimeEntriesBefore.size +
                        newlyMaterializedCatalogKeys.size,
            actual =
                runtimeEntriesAfter.size,
            message =
                "Runtime entry count does not reflect the newly " +
                        "materialized rejected-strong mappings."
        )

        val snapshot =
            DefaultNutritionKnowledgeSnapshotReader(
                catalogFile =
                    files.catalogFile,
                exactMappingFile =
                    files.exactMappingFile,
                runtimeNutritionFile =
                    files.runtimeNutritionFile,
                mappingFile =
                    files.outputMappingFile
            )
                .read()

        assertEquals(
            expected =
                runtimeEntriesAfter.size,
            actual =
                snapshot.runtimeEntryCount,
            message =
                "Snapshot runtime entry count differs from the " +
                        "materialized nutrition runtime artifact."
        )

        assertEquals(
            expected =
                snapshot.coveredCatalogItemCount,
            actual =
                snapshot.runtimeEntryCount,
            message =
                "Runtime entries must equal covered catalog items."
        )

        assertEquals(
            expected =
                snapshot.exactMatchCount +
                        snapshot.mappedMatchCount,
            actual =
                snapshot.runtimeEntryCount,
            message =
                "Runtime entries must equal exact plus mapped matches."
        )

        printSummary(
            runtimeEntryCountBefore =
                runtimeEntriesBefore.size,
            runtimeEntryCountAfter =
                runtimeEntriesAfter.size,
            newlyMaterializedCatalogKeys =
                newlyMaterializedCatalogKeys,
            snapshot =
                snapshot,
            runtimeFile =
                files.runtimeNutritionFile
        )
    }

    private fun requireInputFiles(
        files:
        NutritionKnowledgeRebuildProjectFiles
    ) {
        require(files.catalogFile.isFile) {
            "Catalog file does not exist: " +
                    files.catalogFile.absolutePath
        }

        require(files.serverArtifactDirectory.isDirectory) {
            "Server artifact directory does not exist: " +
                    files.serverArtifactDirectory.absolutePath
        }

        require(files.serverNutritionFile.isFile) {
            "Server nutrition artifact does not exist: " +
                    files.serverNutritionFile.absolutePath
        }

        require(files.outputMappingFile.isFile) {
            "Catalog-server mapping file does not exist: " +
                    files.outputMappingFile.absolutePath
        }

        require(files.exactMappingFile.isFile) {
            "Exact nutrition mapping file does not exist: " +
                    files.exactMappingFile.absolutePath
        }
    }

    private fun readNutritionMappings(
        file: File
    ): Map<String, String> {

        val root =
            parseObject(
                file =
                    file,
                sourceName =
                    "catalog-server mapping file"
            )

        val mappings =
            root["mappings"]
                ?.takeIf {
                    it.isJsonArray
                }
                ?.asJsonArray
                ?: error(
                    "Catalog-server mapping file contains no " +
                            "'mappings' array: " +
                            file.absolutePath
                )

        val result =
            linkedMapOf<String, String>()

        mappings.forEach { element ->

            require(element.isJsonObject) {
                "Catalog-server mapping entry must be a JSON object."
            }

            val mapping =
                element.asJsonObject

            val serverArtifact =
                mapping.optionalString(
                    key =
                        "serverArtifact"
                )
                    ?: mapping.optionalString(
                        key =
                            "sourceArtifact"
                    )
                    ?: NUTRITION_ARTIFACT

            if (
                serverArtifact !=
                NUTRITION_ARTIFACT
            ) {
                return@forEach
            }

            val catalogKey =
                normalizeKey(
                    value =
                        mapping.requiredString(
                            key =
                                "catalogKey"
                        )
                )

            val serverKey =
                normalizeKey(
                    value =
                        mapping.requiredString(
                            key =
                                "serverKey"
                        )
                )

            val existing =
                result.put(
                    catalogKey,
                    serverKey
                )

            require(existing == null) {
                "Duplicate nutrition mapping for catalog key " +
                        "'$catalogKey'."
            }
        }

        return result.toSortedMap()
    }

    private fun readRuntimeEntries(
        file: File
    ): Map<String, JsonElement> {

        if (!file.isFile) {
            return emptyMap()
        }

        val root =
            parseObject(
                file =
                    file,
                sourceName =
                    "runtime nutrition artifact"
            )

        val entries =
            root["entries"]
                ?.takeIf {
                    it.isJsonObject
                }
                ?.asJsonObject
                ?: error(
                    "Runtime nutrition artifact contains no " +
                            "'entries' object: " +
                            file.absolutePath
                )

        return entries
            .entrySet()
            .associate { (
                             key,
                             value
                         ) ->

                normalizeKey(
                    value =
                        key
                ) to
                        value
            }
            .toSortedMap()
    }

    private fun parseObject(
        file: File,
        sourceName: String
    ): JsonObject {

        require(file.isFile) {
            "$sourceName does not exist: " +
                    file.absolutePath
        }

        val root =
            JsonParser.parseString(
                file.readText()
            )

        require(root.isJsonObject) {
            "$sourceName must contain a JSON object: " +
                    file.absolutePath
        }

        return root.asJsonObject
    }

    private fun JsonObject.requiredString(
        key: String
    ): String {

        val value =
            optionalString(
                key =
                    key
            )

        require(!value.isNullOrBlank()) {
            "Missing or blank string '$key'."
        }

        return value
    }

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

    private fun normalizeKey(
        value: String
    ): String =
        value
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

    private fun printSummary(
        runtimeEntryCountBefore: Int,
        runtimeEntryCountAfter: Int,
        newlyMaterializedCatalogKeys: List<String>,
        snapshot:
        de.shopme.tools.knowledge.rebuild.nutrition
        .NutritionKnowledgeRebuildSnapshot,
        runtimeFile: File
    ) {
        println()
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
        println(
            "REJECTED STRONG NUTRITION RUNTIME APPLICATION"
        )
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
        println(
            "Runtime entries before : " +
                    runtimeEntryCountBefore
        )
        println(
            "Runtime entries after  : " +
                    runtimeEntryCountAfter
        )
        println(
            "Runtime entries added  : " +
                    (
                            runtimeEntryCountAfter -
                                    runtimeEntryCountBefore
                            )
        )
        println(
            "Exact matches          : " +
                    snapshot.exactMatchCount
        )
        println(
            "Mapped matches         : " +
                    snapshot.mappedMatchCount
        )
        println(
            "Covered catalog items  : " +
                    snapshot.coveredCatalogItemCount
        )
        println(
            "Missing catalog items  : " +
                    snapshot.missingCatalogItemCount
        )
        println(
            "Coverage               : " +
                    String.format(
                        "%.4f%%",
                        snapshot.coverage *
                                100.0
                    )
        )
        println(
            "Newly materialized     : " +
                    newlyMaterializedCatalogKeys.size
        )

        newlyMaterializedCatalogKeys
            .forEach {
                println(
                    "  + $it"
                )
            }

        println(
            "Runtime artifact       : " +
                    runtimeFile.absolutePath
        )
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
    }

    private fun findProjectRoot():
            File {

        val workingDirectory =
            File(
                requireNotNull(
                    System.getProperty(
                        "user.dir"
                    )
                ) {
                    "System property 'user.dir' is unavailable."
                }
            )
                .absoluteFile

        return generateSequence(
            seed =
                workingDirectory
        ) {
            it.parentFile
        }
            .firstOrNull { candidate ->

                File(
                    candidate,
                    "app"
                )
                    .isDirectory &&
                        File(
                            candidate,
                            "data"
                        )
                            .isDirectory
            }
            ?: error(
                "Could not locate ShopMe project root from: " +
                        workingDirectory.absolutePath
            )
    }

    private data class ExpectedRuntimeMapping(
        val catalogKey: String,
        val serverKey: String
    )

    private companion object {

        const val NUTRITION_ARTIFACT =
            "nutrition.json"

        val WHITESPACE_REGEX =
            Regex("\\s+")

        val EXPECTED_RUNTIME_MAPPINGS =
            listOf(
                ExpectedRuntimeMapping(
                    catalogKey =
                        "fresh whole grain pasta",
                    serverKey =
                        "whole grain pasta"
                ),
                ExpectedRuntimeMapping(
                    catalogKey =
                        "organic canned white beans",
                    serverKey =
                        "organic white beans"
                ),
                ExpectedRuntimeMapping(
                    catalogKey =
                        "organic egg noodles",
                    serverKey =
                        "organic wide egg noodles"
                )
            )
    }
}