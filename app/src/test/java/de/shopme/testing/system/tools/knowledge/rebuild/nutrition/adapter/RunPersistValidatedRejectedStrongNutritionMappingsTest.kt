package de.shopme.testing.system.tools.knowledge.rebuild.nutrition.adapter

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import de.shopme.tools.knowledge.rebuild.nutrition.adapter.PersistValidatedRejectedStrongNutritionMappings
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunPersistValidatedRejectedStrongNutritionMappingsTest {

    @Test
    fun persistValidatedRejectedStrongNutritionMappings() {

        val projectRoot =
            findProjectRoot()

        val validationFile =
            File(
                projectRoot,
                "data/generated/knowledge/reports/" +
                        "nutrition.rejected-strong-" +
                        "candidate-validation.json"
            )

        val mappingFile =
            File(
                projectRoot,
                "data/generated/knowledge/mappings/" +
                        "catalog-server.mappings.json"
            )

        val expectedAcceptedMappings =
            readAcceptedMappings(
                validationFile =
                    validationFile
            )

        assertEquals(
            expected =
                EXPECTED_ACCEPTED_MAPPING_COUNT,
            actual =
                expectedAcceptedMappings.size,
            message =
                "The current rejected-strong validation report should " +
                        "contain exactly three accepted mappings."
        )

        val beforeMappings =
            readNutritionMappings(
                mappingFile =
                    mappingFile
            )

        val result =
            PersistValidatedRejectedStrongNutritionMappings()
                .run(
                    validationFile =
                        validationFile,
                    mappingFile =
                        mappingFile
                )

        val afterMappings =
            readNutritionMappings(
                mappingFile =
                    mappingFile
            )

        assertEquals(
            expected =
                EXPECTED_ACCEPTED_MAPPING_COUNT,
            actual =
                result.acceptedValidationCount
        )

        assertEquals(
            expected =
                0,
            actual =
                result.rejectedValidationCount
        )

        assertEquals(
            expected =
                result.acceptedValidationCount,
            actual =
                result.addedMappingCount +
                        result.unchangedMappingCount
        )

        assertEquals(
            expected =
                beforeMappings.size,
            actual =
                result.existingMappingCount
        )

        assertEquals(
            expected =
                afterMappings.size,
            actual =
                result.finalMappingCount
        )

        assertEquals(
            expected =
                beforeMappings.size +
                        result.addedMappingCount,
            actual =
                afterMappings.size
        )

        expectedAcceptedMappings
            .forEach { expected ->

                assertEquals(
                    expected =
                        expected.serverKey,
                    actual =
                        afterMappings[
                            expected.catalogKey
                        ],
                    message =
                        "Accepted rejected-strong nutrition mapping " +
                                "was not persisted correctly for " +
                                "'${expected.catalogKey}'."
                )
            }

        assertTrue(
            actual =
                mappingFile.isFile,
            message =
                "Catalog-server mapping file was not written."
        )
    }

    private fun readAcceptedMappings(
        validationFile: File
    ): List<ExpectedMapping> {

        require(validationFile.isFile) {
            "Validation file does not exist: " +
                    validationFile.absolutePath
        }

        val root =
            parseObject(
                file =
                    validationFile
            )

        val entries =
            root["entries"]
                ?.takeIf {
                    it.isJsonArray
                }
                ?.asJsonArray
                ?: error(
                    "Validation report contains no 'entries' array."
                )

        return entries
            .mapNotNull { element ->

                require(element.isJsonObject) {
                    "Validation entry must be a JSON object."
                }

                val entry =
                    element.asJsonObject

                val accepted =
                    entry["accepted"]
                        ?.takeIf {
                            it.isJsonPrimitive &&
                                    it.asJsonPrimitive.isBoolean
                        }
                        ?.asBoolean
                        ?: error(
                            "Validation entry contains no boolean " +
                                    "'accepted'."
                        )

                if (!accepted) {
                    return@mapNotNull null
                }

                ExpectedMapping(
                    catalogKey =
                        normalizeKey(
                            entry.requiredString(
                                key =
                                    "catalogKey"
                            )
                        ),
                    serverKey =
                        normalizeKey(
                            entry.requiredString(
                                key =
                                    "selectedServerKey"
                            )
                        )
                )
            }
            .sortedBy {
                it.catalogKey
            }
    }

    private fun readNutritionMappings(
        mappingFile: File
    ): Map<String, String> {

        require(mappingFile.isFile) {
            "Mapping file does not exist: " +
                    mappingFile.absolutePath
        }

        val root =
            parseObject(
                file =
                    mappingFile
            )

        val mappings =
            root["mappings"]
                ?.takeIf {
                    it.isJsonArray
                }
                ?.asJsonArray
                ?: error(
                    "Mapping file contains no 'mappings' array."
                )

        val result =
            linkedMapOf<String, String>()

        mappings.forEach { element ->

            require(element.isJsonObject) {
                "Mapping entry must be a JSON object."
            }

            val mapping =
                element.asJsonObject

            val serverArtifact =
                mapping["serverArtifact"]
                    ?.takeIf {
                        !it.isJsonNull &&
                                it.isJsonPrimitive &&
                                it.asJsonPrimitive.isString
                    }
                    ?.asString
                    ?.trim()
                    ?: NUTRITION_ARTIFACT

            if (serverArtifact != NUTRITION_ARTIFACT) {
                return@forEach
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

            require(
                result.put(
                    catalogKey,
                    serverKey
                ) ==
                        null
            ) {
                "Duplicate nutrition catalog-server mapping for " +
                        "'$catalogKey'."
            }
        }

        return result.toSortedMap()
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

    private fun JsonObject.requiredString(
        key: String
    ): String {

        val value =
            get(key)

        require(
            value != null &&
                    !value.isJsonNull &&
                    value.isJsonPrimitive &&
                    value.asJsonPrimitive.isString
        ) {
            "Missing string '$key'."
        }

        return value
            .asString
            .trim()
            .also {
                require(it.isNotBlank()) {
                    "String '$key' must not be blank."
                }
            }
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

    private data class ExpectedMapping(
        val catalogKey: String,
        val serverKey: String
    )

    private companion object {

        const val EXPECTED_ACCEPTED_MAPPING_COUNT =
            3

        const val NUTRITION_ARTIFACT =
            "nutrition.json"

        val WHITESPACE_REGEX =
            Regex("\\s+")
    }
}