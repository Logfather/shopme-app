package de.shopme.tools.knowledge.carbon.loader

import java.io.File

class CarbonReferenceLoader {

    private val entriesBlockRegex =
        """"entries"\s*:\s*\{([\s\S]*)}""".toRegex()

    private val referenceRegex =
        """"([^"]+)"\s*:""".toRegex()

    fun load(
        file: File
    ): Set<String> {

        val content =
            file.readText()

        val entriesBlock =
            entriesBlockRegex
                .find(content)
                ?.groupValues
                ?.get(1)
                ?: return emptySet()

        return referenceRegex
            .findAll(entriesBlock)
            .map { match ->
                match.groupValues[1]
            }
            .filter { reference ->
                reference != "kgCo2ePerKg" &&
                        reference != "source"
            }
            .toSet()
    }
}