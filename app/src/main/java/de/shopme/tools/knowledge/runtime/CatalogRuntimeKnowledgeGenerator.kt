package de.shopme.tools.knowledge.runtime

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import java.io.File

class CatalogRuntimeKnowledgeGenerator(
    private val gson: Gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .create(),
    private val printLine: (String) -> Unit =
        ::println
) {

    fun generate(
        catalogFile: File,
        serverArtifactDirectory: File,
        runtimeArtifactDirectory: File,
        catalogServerMappingFile: File?
    ): CatalogRuntimeKnowledgeGenerationReport {

        require(catalogFile.isFile) {
            "Catalog file missing: ${catalogFile.absolutePath}"
        }

        require(serverArtifactDirectory.isDirectory) {
            "Server knowledge directory missing: " +
                    serverArtifactDirectory.absolutePath
        }

        ensureDirectoryExists(
            directory = runtimeArtifactDirectory
        )

        val catalogKeys =
            readCatalogKeys(
                file = catalogFile
            )

        val mappings =
            readMappings(
                file = catalogServerMappingFile
            )

        val serverFiles =
            serverArtifactDirectory
                .listFiles { file ->
                    file.isFile &&
                            file.extension.equals(
                                other = "json",
                                ignoreCase = true
                            )
                }
                ?.sortedBy {
                    it.name
                }
                .orEmpty()

        require(serverFiles.isNotEmpty()) {
            "No server artifacts found in " +
                    serverArtifactDirectory.absolutePath
        }

        printLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        printLine("CATALOG RUNTIME KNOWLEDGE BUILD")
        printLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        printLine("Catalog keys=${catalogKeys.size}")
        printLine("Catalog-server mappings=${mappings.size}")

        val artifactReports =
            serverFiles.map { serverFile ->

                generateArtifact(
                    serverFile = serverFile,
                    runtimeDirectory =
                        runtimeArtifactDirectory,
                    catalogKeys =
                        catalogKeys,
                    mappings =
                        mappings
                )
            }

        return CatalogRuntimeKnowledgeGenerationReport(
            catalogKeyCount =
                catalogKeys.size,
            mappingCount =
                mappings.size,
            artifacts =
                artifactReports
        )
    }


    private fun generateArtifact(
        serverFile: File,
        runtimeDirectory: File,
        catalogKeys: Set<String>,
        mappings: Map<String, String>
    ): CatalogRuntimeKnowledgeArtifactReport {

        val serverEntries =
            readEntries(
                file = serverFile
            )

        val normalizedServerEntries =
            serverEntries.entries
                .associateBy(
                    keySelector = {
                        normalizeKey(
                            value = it.key
                        )
                    },
                    valueTransform = {
                        it
                    }
                )

        val runtimeEntries =
            sortedMapOf<String, JsonElement>()

        var exactMatchCount =
            0

        var mappedMatchCount =
            0

        catalogKeys.forEach { catalogKey ->

            val exactEntry =
                normalizedServerEntries[
                    catalogKey
                ]

            if (exactEntry != null) {

                runtimeEntries[
                    catalogKey
                ] = exactEntry.value.deepCopy()

                exactMatchCount++

                return@forEach
            }

            val mappedServerKey =
                mappings[
                    catalogKey
                ]
                    ?: return@forEach

            val mappedEntry =
                normalizedServerEntries[
                    normalizeKey(
                        value = mappedServerKey
                    )
                ]
                    ?: return@forEach

            /*
             * Der Runtime-Key bleibt der Catalog-Key.
             *
             * Nur der Knowledge-Wert wird über den gemappten
             * Server-Key aufgelöst.
             */
            runtimeEntries[
                catalogKey
            ] = mappedEntry.value.deepCopy()

            mappedMatchCount++
        }

        val runtimeFile =
            File(
                runtimeDirectory,
                serverFile.name
            )

        writeRuntimeArtifact(
            entries = runtimeEntries,
            file = runtimeFile
        )

        val missingCount =
            catalogKeys.size -
                    runtimeEntries.size

        printLine("")
        printLine(serverFile.name)
        printLine("server entries=${serverEntries.size}")
        printLine("exact matches=$exactMatchCount")
        printLine("mapped matches=$mappedMatchCount")
        printLine("runtime entries=${runtimeEntries.size}")
        printLine("missing=$missingCount")
        printLine("runtime artifact=${runtimeFile.path}")

        return CatalogRuntimeKnowledgeArtifactReport(
            artifact =
                serverFile.name,
            serverEntryCount =
                serverEntries.size,
            exactMatchCount =
                exactMatchCount,
            mappedMatchCount =
                mappedMatchCount,
            runtimeEntryCount =
                runtimeEntries.size,
            missingCount =
                missingCount,
            runtimeFile =
                runtimeFile.path
        )
    }


    private fun readCatalogKeys(
        file: File
    ): Set<String> {

        val type =
            object :
                TypeToken<List<JsonObject>>() {
            }.type

        val items =
            gson.fromJson<List<JsonObject>>(
                file.readText(),
                type
            )

        return items
            .mapNotNull { item ->
                item.string("normalizedEnglish")
                    ?: item.string("name")
                    ?: item.string("productName")
                    ?: item.string("title")
            }
            .map {
                normalizeKey(
                    value = it
                )
            }
            .filter(String::isNotBlank)
            .toSortedSet()
    }


    private fun readMappings(
        file: File?
    ): Map<String, String> {

        if (
            file == null ||
            !file.isFile
        ) {
            return emptyMap()
        }

        val root =
            JsonParser.parseString(
                file.readText()
            )

        require(root.isJsonObject) {
            "Catalog-server mapping file must contain a JSON object: " +
                    file.absolutePath
        }

        val mappings =
            root.asJsonObject["mappings"]
                ?.takeIf {
                    it.isJsonArray
                }
                ?.asJsonArray
                ?: return emptyMap()

        val parsedMappings =
            mappings.map { element ->

                val mapping =
                    element.asJsonObject

                val catalogKey =
                    normalizeKey(
                        value =
                            mapping.requiredString(
                                key = "catalogKey"
                            )
                    )

                val serverKey =
                    mapping.requiredString(
                        key = "serverKey"
                    )

                catalogKey to serverKey
            }

        val duplicateCatalogKeys =
            parsedMappings
                .groupingBy {
                    it.first
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicateCatalogKeys.isEmpty()) {
            "Duplicate catalog-server mappings: " +
                    duplicateCatalogKeys.sorted()
        }

        return parsedMappings
            .toMap()
            .toSortedMap()
    }


    private fun readEntries(
        file: File
    ): Map<String, JsonElement> {

        val root =
            JsonParser.parseString(
                file.readText()
            )

        require(root.isJsonObject) {
            "Server artifact must contain a JSON object: " +
                    file.absolutePath
        }

        val entries =
            root.asJsonObject["entries"]
                ?.takeIf {
                    it.isJsonObject
                }
                ?.asJsonObject
                ?: return emptyMap()

        return entries
            .entrySet()
            .associate { entry ->
                entry.key to entry.value
            }
    }


    private fun writeRuntimeArtifact(
        entries: Map<String, JsonElement>,
        file: File
    ) {

        ensureDirectoryExists(
            directory =
                requireNotNull(
                    file.parentFile
                ) {
                    "Runtime artifact has no parent directory: " +
                            file.absolutePath
                }
        )

        val root =
            JsonObject()

        val entryObject =
            JsonObject()

        entries
            .toSortedMap()
            .forEach { (key, value) ->
                entryObject.add(
                    key,
                    value
                )
            }

        root.add(
            "entries",
            entryObject
        )

        file.writeText(
            gson.toJson(root)
        )
    }


    private fun ensureDirectoryExists(
        directory: File
    ) {

        if (!directory.exists()) {
            check(directory.mkdirs()) {
                "Could not create directory: " +
                        directory.absolutePath
            }
        }

        require(directory.isDirectory) {
            "Path is not a directory: " +
                    directory.absolutePath
        }
    }


    private fun JsonObject.string(
        key: String
    ): String? =
        get(key)
            ?.takeIf {
                !it.isJsonNull &&
                        it.isJsonPrimitive
            }
            ?.asString
            ?.trim()
            ?.takeIf(String::isNotBlank)


    private fun JsonObject.requiredString(
        key: String
    ): String =
        string(key)
            ?: error(
                "Missing or blank string '$key'"
            )


    private fun normalizeKey(
        value: String
    ): String =
        value
            .trim()
            .lowercase()
            .replace("-", " ")
            .replace("_", " ")
            .collapseWhitespace()
            .trim()


    private fun String.collapseWhitespace():
            String {

        val builder =
            StringBuilder(length)

        var previousWasWhitespace =
            false

        for (char in this) {

            if (char.isWhitespace()) {

                if (!previousWasWhitespace) {
                    builder.append(' ')
                }

                previousWasWhitespace =
                    true

            } else {

                builder.append(char)

                previousWasWhitespace =
                    false
            }
        }

        return builder.toString()
    }
}


data class CatalogRuntimeKnowledgeGenerationReport(
    val catalogKeyCount: Int,
    val mappingCount: Int,
    val artifacts: List<CatalogRuntimeKnowledgeArtifactReport>
)


data class CatalogRuntimeKnowledgeArtifactReport(
    val artifact: String,
    val serverEntryCount: Int,
    val exactMatchCount: Int,
    val mappedMatchCount: Int,
    val runtimeEntryCount: Int,
    val missingCount: Int,
    val runtimeFile: String
)