package de.shopme.tools.knowledge.compiler

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

object MergeSupermarketTranslations {

    @JvmStatic
    fun main(args: Array<String>) {

        val baseFile =
            File("../data/raw/catalog/supermarket-translations.json")

        val additionFile =
            File("../data/generated/supermarket-translations-template.json")

        require(baseFile.exists()) {
            "Base translations missing: ${baseFile.absolutePath}"
        }

        require(additionFile.exists()) {
            "Additional translations missing: ${additionFile.absolutePath}"
        }

        val gson =
            GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()

        val type =
            object : TypeToken<LinkedHashMap<String, String>>() {}.type

        val base =
            gson.fromJson<LinkedHashMap<String, String>>(
                baseFile.readText(),
                type
            )

        val additions =
            gson.fromJson<LinkedHashMap<String, String>>(
                additionFile.readText(),
                type
            )

        var added = 0
        var updated = 0
        var ignored = 0

        additions.forEach { (key, value) ->

            if (value.isBlank()) {
                ignored++
                return@forEach
            }

            val old =
                base[key]

            if (old == null || old.isBlank()) {
                base[key] = value
                added++
            } else if (old != value) {
                base[key] = value
                updated++
            }
        }

        val sorted =
            linkedMapOf<String, String>()

        base.keys
            .sorted()
            .forEach {
                sorted[it] = base[it]!!
            }

        baseFile.writeText(
            gson.toJson(sorted)
        )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("SUPERMARKET TRANSLATIONS MERGED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Base      : ${baseFile.absolutePath}")
        println("Addition  : ${additionFile.absolutePath}")
        println("Added     : $added")
        println("Updated   : $updated")
        println("Ignored   : $ignored")
        println("Total     : ${sorted.size}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}