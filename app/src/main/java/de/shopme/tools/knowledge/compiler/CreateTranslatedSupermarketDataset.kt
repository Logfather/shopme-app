package de.shopme.tools.knowledge.compiler

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.stream.JsonReader
import java.io.File

object CreateTranslatedSupermarketDataset {

    @JvmStatic
    fun main(args: Array<String>) {
        val inputFile =
            if (args.isNotEmpty()) File(args[0])
            else File("../data/raw/catalog/supermarket_dataset.json")

        val translationFile =
            if (args.size >= 2) File(args[1])
            else File("../data/raw/catalog/supermarket-translations.json")

        val outputFile =
            if (args.size >= 3) File(args[2])
            else File("../data/raw/catalog/supermarket_dataset.translated.json")

        val missingTranslationsFile =
            if (args.size >= 4) File(args[3])
            else File("../data/generated/missing-supermarket-translations.txt")

        require(inputFile.exists()) {
            "Supermarket dataset not found: ${inputFile.absolutePath}"
        }

        require(translationFile.exists()) {
            "Translation file not found: ${translationFile.absolutePath}"
        }

        outputFile.parentFile?.mkdirs()
        missingTranslationsFile.parentFile?.mkdirs()

        val gson =
            GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()

        val translations =
            readTranslations(
                file = translationFile
            )

        val outputItems =
            mutableListOf<JsonObject>()

        val missingTranslations =
            linkedSetOf<String>()

        var read = 0
        var translated = 0
        var alreadyTranslated = 0

        JsonReader(inputFile.reader()).use { reader ->
            reader.beginArray()

            while (reader.hasNext()) {
                val item =
                    gson.fromJson<JsonObject>(
                        reader,
                        JsonObject::class.java
                    )

                read++

                val normalized =
                    item.string("normalized")

                val existingEnglish =
                    item.string("normalizedEnglish")

                if (existingEnglish != null) {
                    alreadyTranslated++
                    outputItems.add(item)
                    continue
                }

                if (normalized == null) {
                    outputItems.add(item)
                    continue
                }

                val english =
                    translations[normalized]

                if (english != null) {
                    item.addProperty("normalizedEnglish", english)
                    translated++
                } else {
                    missingTranslations.add(normalized)
                }

                outputItems.add(item)
            }

            reader.endArray()
        }

        outputFile.writeText(
            gson.toJson(outputItems)
        )

        writeMissingTranslationsReport(
            file = missingTranslationsFile,
            keys = missingTranslations
        )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("SUPERMARKET DATASET TRANSLATION FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Input              : ${inputFile.absolutePath}")
        println("Translations       : ${translationFile.absolutePath}")
        println("Output             : ${outputFile.absolutePath}")
        println("Missing report     : ${missingTranslationsFile.absolutePath}")
        println("Read items          : $read")
        println("Already translated  : $alreadyTranslated")
        println("New translations    : $translated")
        println("Missing translations: ${missingTranslations.size}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        if (missingTranslations.isNotEmpty()) {
            println("Missing translation keys:")
            missingTranslations.sorted().forEach {
                println("  $it")
            }
        }
    }

    private fun readTranslations(
        file: File
    ): Map<String, String> {
        val result =
            linkedMapOf<String, String>()

        JsonReader(file.reader()).use { reader ->
            reader.beginObject()

            while (reader.hasNext()) {
                val german =
                    reader.nextName()
                        .trim()
                        .lowercase()

                val english =
                    reader.nextString()
                        .trim()
                        .lowercase()

                if (german.isNotBlank() && english.isNotBlank()) {
                    result[german] = english
                }
            }

            reader.endObject()
        }

        return result
    }

    private fun writeMissingTranslationsReport(
        file: File,
        keys: Set<String>
    ) {
        file.parentFile?.mkdirs()

        file.writeText(
            keys
                .sorted()
                .joinToString(separator = "\n")
        )
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
}