package de.shopme.tools.knowledge.compiler

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.File

object CreateRuntimeFoodsJson {

    private data class RuntimeRequiredItem(
        val originalKey: String,
        val matchKey: String,
        val translated: Boolean
    )

    @JvmStatic
    fun main(args: Array<String>) {
        val completeFoodsFile =
            if (args.isNotEmpty()) File(args[0])
            else File("../data/generated/foods.complete.json")

        val supermarketDatasetFile =
            if (args.size >= 2) File(args[1])
            else File("../data/raw/catalog/supermarket_dataset.translated.json")

        val outputFile =
            if (args.size >= 3) File(args[2])
            else File("../data/generated/foods.runtime.json")

        val runtimeAliasesFile =
            if (args.size >= 4) File(args[3])
            else File("../data/raw/catalog/runtime-food-aliases.json")

        require(completeFoodsFile.exists()) {
            "Complete foods catalog not found: ${completeFoodsFile.absolutePath}"
        }

        require(supermarketDatasetFile.exists()) {
            "Supermarket dataset not found: ${supermarketDatasetFile.absolutePath}"
        }

        require(runtimeAliasesFile.exists()) {
            "Runtime aliases file not found: ${runtimeAliasesFile.absolutePath}"
        }

        outputFile.parentFile?.mkdirs()

        val gson = Gson()

        val runtimeAliases =
            readRuntimeAliases(
                file = runtimeAliasesFile
            )

        val requiredItems =
            readSupermarketKeys(
                file = supermarketDatasetFile,
                gson = gson
            )

        val requiredKeys =
            requiredItems
                .map { it.matchKey }
                .toSet()

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 RUNTIME FOODS BUILD")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Complete   : ${completeFoodsFile.absolutePath}")
        println("Dataset    : ${supermarketDatasetFile.absolutePath}")
        println("Aliases    : ${runtimeAliasesFile.absolutePath}")
        println("Output     : ${outputFile.absolutePath}")
        println("Required   : ${requiredKeys.size}")
        println("Alias count: ${runtimeAliases.size}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        val matchedKeys =
            linkedSetOf<String>()

        val sampleCatalogKeys =
            mutableListOf<String>()

        val aliasTargets =
            runtimeAliases.values.toSet()

        val resolvedAliasTargets =
            linkedSetOf<String>()

        var written = 0L
        var scanned = 0L

        outputFile.writer().use { fileWriter ->
            JsonWriter(fileWriter).use { writer ->
                writer.beginArray()

                JsonReader(completeFoodsFile.reader()).use { reader ->
                    reader.beginArray()

                    while (reader.hasNext()) {
                        val item =
                            gson.fromJson<JsonObject>(
                                reader,
                                JsonObject::class.java
                            )

                        scanned++

                        val keys =
                            completeCatalogKeys(item)

                        resolvedAliasTargets.addAll(
                            keys.filter { key ->
                                key in aliasTargets
                            }
                        )

                        if (sampleCatalogKeys.size < 20) {
                            sampleCatalogKeys.addAll(
                                keys.take(20 - sampleCatalogKeys.size)
                            )
                        }

                        val matchedKey =
                            findMatchedKey(
                                keys = keys,
                                requiredKeys = requiredKeys,
                                runtimeAliases = runtimeAliases
                            )

                        if (matchedKey != null && matchedKey !in matchedKeys) {
                            gson.toJson(
                                item,
                                JsonObject::class.java,
                                writer
                            )

                            matchedKeys.add(matchedKey)
                            written++
                        }
                    }

                    reader.endArray()
                }

                writer.endArray()
            }
        }

        val unresolvedAliasReport =
            File("../data/generated/unresolved-runtime-food-aliases.txt")

        unresolvedAliasReport.parentFile?.mkdirs()

        val unresolvedAliases =
            runtimeAliases
                .filter { (_, target) ->
                    target !in resolvedAliasTargets
                }
                .toSortedMap()

        unresolvedAliasReport.writeText(
            unresolvedAliases
                .map { (source, target) ->
                    "$source -> $target"
                }
                .joinToString(separator = "\n")
        )

        val missingRuntimeReport =
            File("../data/generated/missing-runtime-food-keys.txt")

        missingRuntimeReport.parentFile?.mkdirs()

        missingRuntimeReport.writeText(
            requiredKeys
                .filter { key ->
                    key !in matchedKeys
                }
                .sorted()
                .joinToString(separator = "\n")
        )

        val missingCount =
            requiredKeys.size - matchedKeys.size

        val translatedItems =
            requiredItems.filter { it.translated }

        val translatedMatched =
            translatedItems.count {
                it.matchKey in matchedKeys
            }

        val translatedMissing =
            translatedItems.size - translatedMatched

        val untranslatedMissing =
            requiredItems.count {
                !it.translated &&
                        it.matchKey !in matchedKeys
            }

        val missingTranslationKeys =
            requiredItems
                .filter { item ->
                    !item.translated &&
                            item.matchKey !in matchedKeys
                }
                .map { item ->
                    item.originalKey
                }
                .distinct()
                .sorted()

        writeMissingTranslationsReport(
            keys = missingTranslationKeys
        )

        println("Sample required keys:")
        requiredKeys.sorted().take(20).forEach {
            println("  $it")
        }

        println("Sample complete catalog keys:")
        sampleCatalogKeys.sorted().forEach {
            println("  $it")
        }

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("RUNTIME FOODS BUILD FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Required items : ${requiredKeys.size}")
        println("Matched items  : ${matchedKeys.size}")
        println("Missing items  : $missingCount")
        println("Missing report : ${missingRuntimeReport.path}")
        println()
        println("Unresolved aliases : ${unresolvedAliases.size}")
        println("Alias report       : ${unresolvedAliasReport.path}")
        println()
        println("Translation coverage:")
        println("Translated keys        : ${translatedItems.size}")
        println("Translated matched     : $translatedMatched")
        println("Translated missing     : $translatedMissing")
        println("Untranslated missing   : $untranslatedMissing")
        println("Missing translation report: ../data/generated/missing-supermarket-translations.txt")
        println("Written items  : $written")
        println("Scanned items  : $scanned")
        println("Output         : ${outputFile.absolutePath}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    private fun readSupermarketKeys(
        file: File,
        gson: Gson
    ): List<RuntimeRequiredItem> {
        val items =
            mutableListOf<RuntimeRequiredItem>()

        JsonReader(file.reader()).use { reader ->
            reader.beginArray()

            while (reader.hasNext()) {
                val item =
                    gson.fromJson<JsonObject>(
                        reader,
                        JsonObject::class.java
                    )

                val original =
                    item.string("normalized")

                val english =
                    item.string("normalizedEnglish")

                if (original != null) {
                    items.add(
                        RuntimeRequiredItem(
                            originalKey = original,
                            matchKey = english ?: original,
                            translated = english != null
                        )
                    )
                }
            }

            reader.endArray()
        }

        return items
    }

    private fun readRuntimeAliases(
        file: File
    ): Map<String, String> {
        val aliases =
            linkedMapOf<String, String>()

        JsonReader(file.reader()).use { reader ->
            reader.beginObject()

            while (reader.hasNext()) {
                val source =
                    reader.nextName()
                        .trim()
                        .lowercase()

                val target =
                    reader.nextString()
                        .trim()
                        .lowercase()

                if (source.isNotBlank() && target.isNotBlank()) {
                    aliases[source] = target
                }
            }

            reader.endObject()
        }

        return aliases
    }

    private fun findMatchedKey(
        keys: Set<String>,
        requiredKeys: Set<String>,
        runtimeAliases: Map<String, String>
    ): String? {
        keys.firstOrNull { key ->
            key in requiredKeys
        }?.let { directMatch ->
            return directMatch
        }

        return runtimeAliases
            .entries
            .firstOrNull { entry ->
                entry.key in requiredKeys &&
                        entry.value in keys
            }
            ?.key
    }

    private fun writeMissingTranslationsReport(
        keys: List<String>
    ) {
        val file =
            File("../data/generated/missing-supermarket-translations.txt")

        file.parentFile?.mkdirs()

        file.writeText(
            keys.joinToString(separator = "\n")
        )
    }

    private fun completeCatalogKeys(
        item: JsonObject
    ): Set<String> {
        val keys =
            linkedSetOf<String>()

        item.string("normalized")?.let { keys.add(it) }
        item.string("normalizedName")?.let { keys.add(it) }
        item.string("normalized_name")?.let { keys.add(it) }
        item.string("name")?.let { keys.add(it) }
        item.string("itemname")?.let { keys.add(it) }

        item.stringArray("colloquial").forEach { keys.add(it) }
        item.stringArray("autocomplete_tokens").forEach { keys.add(it) }
        item.stringArray("phonetic_tokens").forEach { keys.add(it) }

        return keys
    }

    private fun JsonObject.string(
        key: String
    ): String? {
        val value =
            get(key)
                ?: return null

        if (value.isJsonNull || !value.isJsonPrimitive) {
            return null
        }

        return value.asString
            .trim()
            .lowercase()
            .takeIf { it.isNotBlank() }
    }

    private fun JsonObject.stringArray(
        key: String
    ): List<String> {
        val value =
            get(key)
                ?: return emptyList()

        if (value.isJsonNull || !value.isJsonArray) {
            return emptyList()
        }

        return value.asJsonArray
            .mapNotNull { element ->
                if (element.isJsonNull || !element.isJsonPrimitive) {
                    null
                } else {
                    element.asString
                        .trim()
                        .lowercase()
                        .takeIf { it.isNotBlank() }
                }
            }
    }
}