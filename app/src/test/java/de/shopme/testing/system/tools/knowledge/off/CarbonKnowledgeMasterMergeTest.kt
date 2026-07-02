package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.GsonBuilder
import de.shopme.tools.knowledge.carbon.CarbonKnowledge
import org.junit.Test
import java.io.File
import java.util.TreeMap

class CarbonKnowledgeMasterMergeTest {

    private val gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

    @Test
    fun mergeMasterAndOffCarbonKnowledge() {

        val runtimeFile =
            File(
                "src/main/assets/knowledge/runtime/carbon_footprint.json"
            )

        val masterFile =
            File(
                "src/main/assets/knowledge/runtime/carbon_footprint_master.json"
            )

        val offFile =
            File(
                "data/generated/off/carbon_footprint_final.json"
            )

        if (!masterFile.exists()) {

            require(runtimeFile.exists()) {
                "Carbon runtime file not found: ${runtimeFile.absolutePath}"
            }

            runtimeFile.renameTo(masterFile)

            require(masterFile.exists()) {
                "Failed to rename carbon runtime file to master file: ${masterFile.absolutePath}"
            }
        }

        require(masterFile.exists()) {
            "Carbon master file not found: ${masterFile.absolutePath}"
        }

        require(offFile.exists()) {
            "OFF carbon file not found: ${offFile.absolutePath}"
        }

        val masterKnowledge =
            gson.fromJson(
                masterFile.readText(),
                CarbonKnowledge::class.java
            )

        val offKnowledge =
            gson.fromJson(
                offFile.readText(),
                CarbonKnowledge::class.java
            )

        val mergedEntries =
            TreeMap(
                masterKnowledge.entries
            )

        var added = 0
        var conflicts = 0

        offKnowledge.entries.forEach { (name, footprint) ->

            if (mergedEntries.containsKey(name)) {

                conflicts++
                return@forEach
            }

            mergedEntries[name] =
                footprint

            added++
        }

        val mergedKnowledge =
            CarbonKnowledge(
                entries = mergedEntries
            )

        runtimeFile.parentFile?.mkdirs()

        runtimeFile.writeText(
            gson.toJson(
                mergedKnowledge
            )
        )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 CARBON MASTER MERGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Master entries : ${masterKnowledge.entries.size}")
        println("OFF entries    : ${offKnowledge.entries.size}")
        println("Added          : $added")
        println("Conflicts      : $conflicts")
        println("Final entries  : ${mergedEntries.size}")
        println("Master file    : ${masterFile.absolutePath}")
        println("Runtime output : ${runtimeFile.absolutePath}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}