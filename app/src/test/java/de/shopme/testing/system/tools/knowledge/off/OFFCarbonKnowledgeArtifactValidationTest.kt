package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.GsonBuilder
import de.shopme.tools.knowledge.carbon.CarbonKnowledge
import java.io.File
import kotlin.test.Test

class OFFCarbonKnowledgeArtifactValidationTest {

    private val gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

    @Test
    fun validateCarbonKnowledgeArtifact() {

        val input =
            File(
                "build/off/carbon_footprint_from_off.json"
            )

        require(input.exists()) {
            "OFF carbon artifact not found: ${input.absolutePath}"
        }

        val knowledge =
            gson.fromJson(
                input.readText(),
                CarbonKnowledge::class.java
            )

        val invalid =
            mutableListOf<String>()

        knowledge.entries.forEach { (name, footprint) ->

            if (name.isBlank()) {
                invalid += name
                return@forEach
            }

            if (
                footprint.kilogramsPerKilogram <= 0.0
            ) {
                invalid += name
                return@forEach
            }

            if (
                footprint.kilogramsPerKilogram >= 100.0
            ) {
                invalid += name
                return@forEach
            }
        }

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OFF CARBON KNOWLEDGE VALIDATION")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Entries : ${knowledge.entries.size}")
        println("Invalid : ${invalid.size}")

        if (invalid.isNotEmpty()) {

            println()
            println("Invalid Entries:")

            invalid
                .sorted()
                .take(50)
                .forEach {

                    println("- $it")
                }
        }

        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        check(
            invalid.isEmpty()
        ) {
            "Invalid carbon knowledge entries found: ${invalid.size}"
        }
    }
}