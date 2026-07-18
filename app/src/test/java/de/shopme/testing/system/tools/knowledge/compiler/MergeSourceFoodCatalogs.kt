package de.shopme.tools.knowledge.compiler

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.File

object MergeSourceFoodCatalogs {

    @JvmStatic
    fun main(args: Array<String>) {
        val offCatalogFile =
            if (args.isNotEmpty()) {
                File(args[0])
            } else {
                File("../data/generated/foods.off.json")
            }

        val agribalyseCatalogFile =
            if (args.size >= 2) {
                File(args[1])
            } else {
                File("../data/generated/foods.agribalyse.json")
            }

        val outputFile =
            if (args.size >= 3) {
                File(args[2])
            } else {
                File("../data/generated/foods.complete.json")
            }

        require(offCatalogFile.exists()) {
            "OFF catalog not found: ${offCatalogFile.absolutePath}"
        }

        require(agribalyseCatalogFile.exists()) {
            "Agribalyse catalog not found: ${agribalyseCatalogFile.absolutePath}"
        }

        outputFile.parentFile?.mkdirs()

        val gson = Gson()

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 MERGE SOURCE FOOD CATALOGS")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("OFF        : ${offCatalogFile.absolutePath}")
        println("Agribalyse : ${agribalyseCatalogFile.absolutePath}")
        println("Output     : ${outputFile.absolutePath}")
        println("Mode       : streaming concat")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        var offItems = 0L
        var agribalyseItems = 0L

        outputFile.writer().use { fileWriter ->
            JsonWriter(fileWriter).use { jsonWriter ->
                jsonWriter.beginArray()

                offItems =
                    copyCatalog(
                        inputFile = offCatalogFile,
                        output = jsonWriter,
                        gson = gson,
                        label = "OFF"
                    )

                agribalyseItems =
                    copyCatalog(
                        inputFile = agribalyseCatalogFile,
                        output = jsonWriter,
                        gson = gson,
                        label = "Agribalyse"
                    )

                jsonWriter.endArray()
            }
        }

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("SOURCE FOOD CATALOG MERGE FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("OFF items        : $offItems")
        println("Agribalyse items : $agribalyseItems")
        println("Output items     : ${offItems + agribalyseItems}")
        println("Output           : ${outputFile.absolutePath}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    private fun copyCatalog(
        inputFile: File,
        output: JsonWriter,
        gson: Gson,
        label: String
    ): Long {
        var count = 0L

        JsonReader(inputFile.reader()).use { reader ->
            reader.beginArray()

            while (reader.hasNext()) {
                val item =
                    gson.fromJson<JsonObject>(
                        reader,
                        JsonObject::class.java
                    )

                gson.toJson(
                    item,
                    JsonObject::class.java,
                    output
                )

                count++

                if (count % 100_000 == 0L) {
                    println("$label copied=$count")
                }
            }

            reader.endArray()
        }

        println("$label finished: $count")

        return count
    }
}