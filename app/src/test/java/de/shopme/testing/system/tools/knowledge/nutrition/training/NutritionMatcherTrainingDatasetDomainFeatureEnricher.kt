package de.shopme.testing.system.tools.knowledge.nutrition.training

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File

class NutritionMatcherTrainingDatasetDomainFeatureEnricher(
    private val gson: Gson =
        GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create(),
    private val featureExtractor:
    NutritionDomainMismatchFeatureExtractor =
        NutritionDomainMismatchFeatureExtractor(),
) {

    fun enrich(
        datasetFile: File,
        mismatchReportFile: File,
        outputFile: File = datasetFile,
    ): NutritionMatcherTrainingDatasetDomainFeatureEnrichmentResult {
        require(datasetFile.isFile) {
            "Nutrition matcher training dataset does not exist: " +
                    datasetFile.path
        }

        require(mismatchReportFile.isFile) {
            "Nutrition Food-Domain mismatch report does not exist: " +
                    mismatchReportFile.path
        }

        val datasetRoot =
            parseObject(
                file = datasetFile,
                description =
                    "Nutrition matcher training dataset",
            )

        val mismatchRoot =
            parseObject(
                file = mismatchReportFile,
                description =
                    "Nutrition Food-Domain mismatch report",
            )

        val examples =
            datasetRoot.requireExamplesArray()

        val mismatchIndex =
            buildMismatchIndex(
                mismatchRoot = mismatchRoot,
            )

        var enrichedExampleCount = 0
        var matchedRelationshipCount = 0
        var unmatchedRelationshipCount = 0

        examples.forEachIndexed { index, exampleElement ->
            require(exampleElement.isJsonObject) {
                "Nutrition training example at index $index " +
                        "is not a JSON object."
            }

            val example =
                exampleElement.asJsonObject

            val catalogKey =
                example.requireFirstString(
                    propertyNames =
                        listOf(
                            "catalogKey",
                            "catalogItemKey",
                        ),
                    context =
                        "training example at index $index",
                )

            val serverKey =
                example.requireFirstString(
                    propertyNames =
                        listOf(
                            "serverKey",
                            "candidateServerKey",
                            "selectedServerKey",
                            "knowledgeKey",
                        ),
                    context =
                        "training example at index $index",
                )

            val candidateRank =
                example.findFirstInt(
                    propertyNames =
                        listOf(
                            "candidateRank",
                            "rank",
                        ),
                )

            val mismatchEntry =
                findMismatchEntry(
                    index = mismatchIndex,
                    catalogKey = catalogKey,
                    serverKey = serverKey,
                    candidateRank = candidateRank,
                )

            val features =
                featureExtractor.extract(
                    mismatchEntry = mismatchEntry,
                )

            example.add(
                "domainMismatchFeatures",
                gson.toJsonTree(features),
            )

            enrichedExampleCount += 1

            if (features.reportRelationshipPresent) {
                matchedRelationshipCount += 1
            } else {
                unmatchedRelationshipCount += 1
            }
        }

        datasetRoot.addProperty(
            "domainMismatchFeatureVersion",
            1,
        )

        datasetRoot.add(
            "domainMismatchFeatureCoverage",
            JsonObject().apply {
                addProperty(
                    "exampleCount",
                    enrichedExampleCount,
                )
                addProperty(
                    "matchedRelationshipCount",
                    matchedRelationshipCount,
                )
                addProperty(
                    "unmatchedRelationshipCount",
                    unmatchedRelationshipCount,
                )
            },
        )

        validateEnrichedDataset(
            datasetRoot = datasetRoot,
            expectedExampleCount = examples.size(),
        )

        writeAtomically(
            outputFile = outputFile,
            content = gson.toJson(datasetRoot),
        )

        return NutritionMatcherTrainingDatasetDomainFeatureEnrichmentResult(
            datasetExampleCount = examples.size(),
            enrichedExampleCount = enrichedExampleCount,
            matchedRelationshipCount = matchedRelationshipCount,
            unmatchedRelationshipCount =
                unmatchedRelationshipCount,
            mismatchIndexEntryCount =
                mismatchIndex.values.sumOf { entries ->
                    entries.size
                },
            outputFile = outputFile,
        )
    }

    private fun buildMismatchIndex(
        mismatchRoot: JsonObject,
    ): Map<NutritionDomainMismatchRelationshipKey, List<JsonObject>> {
        val entriesElement =
            mismatchRoot.get("entries")
                ?: error(
                    "Nutrition Food-Domain mismatch report " +
                            "does not contain 'entries'.",
                )

        require(entriesElement.isJsonArray) {
            "Nutrition Food-Domain mismatch report property " +
                    "'entries' is not an array."
        }

        val index =
            linkedMapOf<
                    NutritionDomainMismatchRelationshipKey,
                    MutableList<JsonObject>
                    >()

        entriesElement.asJsonArray
            .forEachIndexed { entryIndex, entryElement ->
                require(entryElement.isJsonObject) {
                    "Mismatch report entry at index $entryIndex " +
                            "is not a JSON object."
                }

                val entry =
                    entryElement.asJsonObject

                val catalogKey =
                    entry.requireString(
                        propertyName = "catalogKey",
                        context =
                            "mismatch entry at index $entryIndex",
                    )

                val serverKey =
                    entry.requireString(
                        propertyName = "serverKey",
                        context =
                            "mismatch entry at index $entryIndex",
                    )

                val key =
                    NutritionDomainMismatchRelationshipKey(
                        catalogKey = normalizeKey(catalogKey),
                        serverKey = normalizeKey(serverKey),
                    )

                index
                    .getOrPut(key) {
                        mutableListOf()
                    }
                    .add(entry)
            }

        return index
            .mapValues { (_, entries) ->
                entries.sortedBy { entry ->
                    entry.findFirstInt(
                        propertyNames =
                            listOf("rank"),
                    ) ?: Int.MAX_VALUE
                }
            }
            .toSortedMap(
                compareBy<NutritionDomainMismatchRelationshipKey>(
                    { key ->
                        key.catalogKey
                    },
                    { key ->
                        key.serverKey
                    },
                ),
            )
    }

    private fun findMismatchEntry(
        index:
        Map<
                NutritionDomainMismatchRelationshipKey,
                List<JsonObject>
                >,
        catalogKey: String,
        serverKey: String,
        candidateRank: Int?,
    ): JsonObject? {
        val relationshipKey =
            NutritionDomainMismatchRelationshipKey(
                catalogKey = normalizeKey(catalogKey),
                serverKey = normalizeKey(serverKey),
            )

        val matchingEntries =
            index[relationshipKey]
                ?: return null

        if (candidateRank == null) {
            return matchingEntries.singleOrNull()
                ?: matchingEntries.first()
        }

        return matchingEntries.firstOrNull { entry ->
            entry.findFirstInt(
                propertyNames =
                    listOf("rank"),
            ) ==
                    candidateRank
        } ?: matchingEntries.singleOrNull()
    }

    private fun validateEnrichedDataset(
        datasetRoot: JsonObject,
        expectedExampleCount: Int,
    ) {
        val examples =
            datasetRoot.requireExamplesArray()

        require(examples.size() == expectedExampleCount) {
            "Nutrition dataset example count changed during " +
                    "Domain-Mismatch enrichment: expected=" +
                    "$expectedExampleCount, actual=${examples.size()}"
        }

        examples.forEachIndexed { index, element ->
            require(element.isJsonObject) {
                "Enriched training example at index $index " +
                        "is not a JSON object."
            }

            val example =
                element.asJsonObject

            require(
                example.has("domainMismatchFeatures"),
            ) {
                "Training example at index $index does not contain " +
                        "'domainMismatchFeatures'."
            }

            val featureElement =
                example.get("domainMismatchFeatures")

            require(featureElement.isJsonObject) {
                "'domainMismatchFeatures' at index $index " +
                        "is not a JSON object."
            }

            val features =
                featureElement.asJsonObject

            require(
                features.get("version")?.asInt == 1,
            ) {
                "Unexpected Domain-Mismatch feature version " +
                        "at training example index $index."
            }
        }
    }

    private fun JsonObject.requireExamplesArray(): JsonArray {
        val examplesElement =
            get("examples")
                ?: error(
                    "Nutrition matcher training dataset does not " +
                            "contain an 'examples' array.",
                )

        require(examplesElement.isJsonArray) {
            "Nutrition matcher training dataset property " +
                    "'examples' is not an array."
        }

        return examplesElement.asJsonArray
    }

    private fun parseObject(
        file: File,
        description: String,
    ): JsonObject {
        val root =
            JsonParser.parseString(
                file.readText(),
            )

        require(root.isJsonObject) {
            "$description root is not a JSON object: ${file.path}"
        }

        return root.asJsonObject
    }

    private fun JsonObject.requireString(
        propertyName: String,
        context: String,
    ): String {
        val element =
            get(propertyName)
                ?: error(
                    "Missing '$propertyName' in $context.",
                )

        require(
            element.isJsonPrimitive &&
                    element.asJsonPrimitive.isString
        ) {
            "Property '$propertyName' in $context is not a string."
        }

        return element.asString
    }

    private fun JsonObject.requireFirstString(
        propertyNames: List<String>,
        context: String,
    ): String {
        propertyNames.forEach { propertyName ->
            val element =
                get(propertyName)
                    ?: return@forEach

            if (
                element.isJsonPrimitive &&
                element.asJsonPrimitive.isString
            ) {
                return element.asString
            }
        }

        error(
            "None of the expected string properties " +
                    "$propertyNames exists in $context. " +
                    "Available properties=${keySet().sorted()}",
        )
    }

    private fun JsonObject.findFirstInt(
        propertyNames: List<String>,
    ): Int? {
        propertyNames.forEach { propertyName ->
            val element =
                get(propertyName)
                    ?: return@forEach

            if (
                element.isJsonPrimitive &&
                element.asJsonPrimitive.isNumber
            ) {
                return element.asInt
            }
        }

        return null
    }

    private fun normalizeKey(
        value: String,
    ): String =
        value
            .trim()
            .lowercase()
            .replace(
                regex = WHITESPACE_REGEX,
                replacement = " ",
            )

    private fun writeAtomically(
        outputFile: File,
        content: String,
    ) {
        outputFile.parentFile?.mkdirs()

        val temporaryFile =
            File(
                outputFile.parentFile,
                "${outputFile.name}.tmp",
            )

        temporaryFile.writeText(content)

        if (outputFile.exists()) {
            require(outputFile.delete()) {
                "Could not replace existing Nutrition training " +
                        "dataset: ${outputFile.path}"
            }
        }

        require(
            temporaryFile.renameTo(outputFile),
        ) {
            "Could not move temporary Nutrition training dataset " +
                    "to ${outputFile.path}"
        }
    }

    private companion object {

        val WHITESPACE_REGEX =
            Regex("\\s+")
    }
}

data class NutritionMatcherTrainingDatasetDomainFeatureEnrichmentResult(
    val datasetExampleCount: Int,
    val enrichedExampleCount: Int,
    val matchedRelationshipCount: Int,
    val unmatchedRelationshipCount: Int,
    val mismatchIndexEntryCount: Int,
    val outputFile: File,
)

private data class NutritionDomainMismatchRelationshipKey(
    val catalogKey: String,
    val serverKey: String,
)