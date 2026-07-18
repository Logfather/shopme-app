package de.shopme.tools.knowledge.compiler

import com.google.gson.GsonBuilder
import java.io.File

object CreateSupermarketTranslationTemplate {

    @JvmStatic
    fun main(args: Array<String>) {

        val inputFile =
            if (args.isNotEmpty()) File(args[0])
            else File("../data/generated/missing-supermarket-translations.txt")

        val outputFile =
            if (args.size >= 2) File(args[1])
            else File("../data/generated/supermarket-translations-template.json")

        require(inputFile.exists()) {
            "Missing translations file not found: ${inputFile.absolutePath}"
        }

        outputFile.parentFile?.mkdirs()

        val template =
            inputFile
                .readLines()
                .map { key ->
                    key.trim()
                }
                .filter { key ->
                    key.isNotBlank()
                }
                .distinct()
                .sorted()
                .associateWith {
                    ""
                }

        val gson =
            GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()

        outputFile.writeText(
            gson.toJson(template)
        )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("TRANSLATION TEMPLATE CREATED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Input  : ${inputFile.absolutePath}")
        println("Output : ${outputFile.absolutePath}")
        println("Keys   : ${template.size}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}