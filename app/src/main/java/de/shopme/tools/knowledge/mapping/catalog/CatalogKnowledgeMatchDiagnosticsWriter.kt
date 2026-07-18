package de.shopme.tools.knowledge.mapping.catalog

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File

data class CatalogKnowledgeMatchDiagnostics(
    val version: Int =
        CatalogKnowledgeMatchDiagnosticsContract.CURRENT_VERSION,
    val diagnostics: List<CatalogKnowledgeMatchDiagnostic>
)

data class CatalogKnowledgeMatchDiagnostic(
    val catalogKey: String,
    val serverArtifact: String,
    val candidateCount: Int,
    val candidateServerKeys: List<String>,
    val decisionType: String?,
    val selectedServerKey: String?,
    val confidence: Double?,
    val decisionReason: String?,
    val validationStatus: String?,
    val validationReason: String?,
    val mappingWritten: Boolean
)

object CatalogKnowledgeMatchDiagnosticsContract {

    const val CURRENT_VERSION: Int = 1
}

/**
 * Führt die bereits persistierten Matching-Artefakte zu einem stabilen
 * Diagnoseartefakt zusammen.
 *
 * Der Writer führt weder Retrieval noch AI-Matching oder Validierung aus.
 */
class CatalogKnowledgeMatchDiagnosticsWriter {

    private val gson =
        GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create()

    fun write(
        requestFile: File,
        decisionFile: File,
        validationReportFile: File,
        mappingFile: File,
        outputFile: File
    ): CatalogKnowledgeMatchDiagnostics {

        require(requestFile.isFile) {
            "Match request file does not exist: ${requestFile.absolutePath}"
        }

        require(decisionFile.isFile) {
            "Match decision file does not exist: ${decisionFile.absolutePath}"
        }

        require(validationReportFile.isFile) {
            "Validation report file does not exist: " +
                    validationReportFile.absolutePath
        }

        require(mappingFile.isFile) {
            "Mapping file does not exist: ${mappingFile.absolutePath}"
        }

        val requests =
            readArray(
                file = requestFile,
                keys =
                    listOf(
                        "requests"
                    )
            )

        val decisionsByIdentity =
            readArray(
                file = decisionFile,
                keys =
                    listOf(
                        "decisions"
                    )
            )
                .associateBy(
                    keySelector = ::identity
                )

        val validationsByIdentity =
            readArray(
                file = validationReportFile,
                keys =
                    listOf(
                        "validations",
                        "results",
                        "entries"
                    )
            )
                .associateBy(
                    keySelector = ::identity
                )

        val writtenMappings =
            readArray(
                file = mappingFile,
                keys =
                    listOf(
                        "mappings"
                    )
            )
                .map(::identity)
                .toSet()

        val diagnostics =
            requests
                .map { request ->

                    val identity =
                        identity(
                            element = request
                        )

                    val decision =
                        decisionsByIdentity[
                            identity
                        ]

                    val validation =
                        validationsByIdentity[
                            identity
                        ]

                    val candidates =
                        request.array("candidates")
                            .mapNotNull { candidate ->
                                candidate
                                    .takeIf(JsonElement::isJsonObject)
                                    ?.asJsonObject
                                    ?.string(
                                        "serverKey",
                                        "key",
                                        "canonicalKey"
                                    )
                            }
                            .map(String::trim)
                            .filter(String::isNotBlank)
                            .distinct()
                            .sorted()

                    CatalogKnowledgeMatchDiagnostic(
                        catalogKey =
                            identity.catalogKey,
                        serverArtifact =
                            identity.serverArtifact,
                        candidateCount =
                            candidates.size,
                        candidateServerKeys =
                            candidates,
                        decisionType =
                            decision?.string(
                                "type",
                                "decisionType",
                                "outcome"
                            ),
                        selectedServerKey =
                            decision?.string(
                                "selectedServerKey",
                                "serverKey"
                            ),
                        confidence =
                            decision?.double(
                                "confidence"
                            ),
                        decisionReason =
                            decision?.string(
                                "reason",
                                "decisionReason"
                            ),
                        validationStatus =
                            validation?.string(
                                "status",
                                "validationStatus",
                                "result"
                            ),
                        validationReason =
                            validation?.string(
                                "reason",
                                "validationReason",
                                "message"
                            ),
                        mappingWritten =
                            identity in writtenMappings
                    )
                }
                .sortedWith(
                    compareBy<CatalogKnowledgeMatchDiagnostic>(
                        { it.serverArtifact },
                        { it.catalogKey }
                    )
                )

        val result =
            CatalogKnowledgeMatchDiagnostics(
                diagnostics = diagnostics
            )

        ensureParentDirectory(
            file = outputFile
        )

        outputFile.writeText(
            gson.toJson(
                result
            )
        )

        return result
    }

    private fun readArray(
        file: File,
        keys: List<String>
    ): List<JsonObject> {

        val root =
            JsonParser.parseString(
                file.readText()
            )

        val array =
            when {
                root.isJsonArray ->
                    root.asJsonArray

                root.isJsonObject ->
                    keys
                        .asSequence()
                        .mapNotNull { key ->
                            root.asJsonObject[
                                key
                            ]
                                ?.takeIf(JsonElement::isJsonArray)
                                ?.asJsonArray
                        }
                        .firstOrNull()
                        ?: JsonArray()

                else ->
                    JsonArray()
            }

        return array
            .mapNotNull { element ->
                element
                    .takeIf(JsonElement::isJsonObject)
                    ?.asJsonObject
            }
    }

    private fun identity(
        element: JsonObject
    ): MatchIdentity =
        MatchIdentity(
            catalogKey =
                element.requiredString(
                    "catalogKey"
                ),
            serverArtifact =
                element.string(
                    "serverArtifact",
                    "sourceArtifact"
                )
                    ?: DEFAULT_SERVER_ARTIFACT
        )

    private fun ensureParentDirectory(
        file: File
    ) {
        val parent =
            requireNotNull(
                file.parentFile
            ) {
                "Diagnostics file has no parent directory: " +
                        file.absolutePath
            }

        if (!parent.exists()) {
            check(parent.mkdirs()) {
                "Could not create diagnostics directory: " +
                        parent.absolutePath
            }
        }

        require(parent.isDirectory) {
            "Diagnostics parent is not a directory: " +
                    parent.absolutePath
        }
    }

    private fun JsonObject.array(
        key: String
    ): JsonArray =
        get(key)
            ?.takeIf(JsonElement::isJsonArray)
            ?.asJsonArray
            ?: JsonArray()

    private fun JsonObject.requiredString(
        key: String
    ): String =
        string(key)
            ?: error(
                "Missing or blank string '$key'"
            )

    private fun JsonObject.string(
        vararg keys: String
    ): String? =
        keys
            .asSequence()
            .mapNotNull { key ->
                get(key)
                    ?.takeIf {
                        !it.isJsonNull &&
                                it.isJsonPrimitive
                    }
                    ?.asString
                    ?.trim()
                    ?.takeIf(String::isNotBlank)
            }
            .firstOrNull()

    private fun JsonObject.double(
        key: String
    ): Double? {

        val value =
            get(key)
                ?.takeIf {
                    !it.isJsonNull &&
                            it.isJsonPrimitive
                }
                ?: return null

        return runCatching {
            value.asDouble
        }
            .getOrNull()
    }

    private data class MatchIdentity(
        val catalogKey: String,
        val serverArtifact: String
    )

    private companion object {

        const val DEFAULT_SERVER_ARTIFACT =
            "nutrition.json"
    }
}