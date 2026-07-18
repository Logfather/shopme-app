package de.shopme.tools.knowledge.compiler

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.File

object MergeOFFFoodsJsonChunks {

    @JvmStatic
    fun main(args: Array<String>) {
        val inputDirectory =
            if (args.isNotEmpty()) File(args[0])
            else File("../data/generated/openfoodfacts/catalog")

        val outputFile =
            if (args.size >= 2) File(args[1])
            else File("../data/generated/foods.off.json")

        outputFile.parentFile?.mkdirs()

        val chunkFiles =
            inputDirectory
                .listFiles { file ->
                    file.isFile &&
                            file.name.startsWith("foods.off.") &&
                            file.name.endsWith(".json")
                }
                .orEmpty()
                .sortedBy { it.name }

        require(chunkFiles.isNotEmpty()) {
            "No foods.off chunk files found in ${inputDirectory.absolutePath}"
        }

        val gson = Gson()

        var totalItems = 0L

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 MERGE OFF FOODS CHUNKS")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Chunks : ${chunkFiles.size}")
        println("Input  : ${inputDirectory.absolutePath}")
        println("Output : ${outputFile.absolutePath}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        outputFile.writer().use { fileWriter ->
            JsonWriter(fileWriter).use { jsonWriter ->
                jsonWriter.beginArray()

                chunkFiles.forEachIndexed { index, file ->
                    var chunkItems = 0L

                    JsonReader(file.reader()).use { jsonReader ->
                        jsonReader.beginArray()

                        while (jsonReader.hasNext()) {
                            val item =
                                gson.fromJson<JsonObject>(
                                    jsonReader,
                                    JsonObject::class.java
                                )

                            gson.toJson(item, JsonObject::class.java, jsonWriter)

                            chunkItems++
                            totalItems++
                        }

                        jsonReader.endArray()
                    }

                    println(
                        "Merged chunk ${index + 1}/${chunkFiles.size}: items=$chunkItems total=$totalItems"
                    )
                }

                jsonWriter.endArray()
            }
        }

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("OFF foods.json merge finished.")
        println("Items  : $totalItems")
        println("Output : ${outputFile.absolutePath}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}