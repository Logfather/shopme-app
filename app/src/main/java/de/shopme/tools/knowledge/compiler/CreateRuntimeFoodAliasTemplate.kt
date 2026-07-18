package de.shopme.tools.knowledge.compiler

import com.google.gson.GsonBuilder
import java.io.File

object CreateRuntimeFoodAliasTemplate {

    @JvmStatic
    fun main(args: Array<String>) {

        val inputFile =
            if (args.isNotEmpty()) File(args[0])
            else File("../data/generated/missing-runtime-food-keys.txt")

        val outputFile =
            if (args.size >= 2) File(args[1])
            else File("../data/generated/runtime-food-aliases-template.json")

        require(inputFile.exists()) {
            "Missing runtime keys file not found: ${inputFile.absolutePath}"
        }

        outputFile.parentFile?.mkdirs()

        val aliases =
            inputFile
                .readLines()
                .map { it.trim().lowercase() }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
                .associateWith { "" }

        val gson =
            GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()

        outputFile.writeText(
            gson.toJson(aliases)
        )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("RUNTIME FOOD ALIAS TEMPLATE CREATED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Input : ${inputFile.absolutePath}")
        println("Output: ${outputFile.absolutePath}")
        println("Keys  : ${aliases.size}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}