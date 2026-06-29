package de.shopme.tools.knowledge.catalog

import java.io.File

class CatalogReferenceLoader {

    private val nutritionReferenceRegex =
        Regex(
            """"nutrition"\s*:\s*\{[^}]*"reference"\s*:\s*"([^"]+)""""
        )

    fun load(
        file: File
    ): Set<String> {

        return nutritionReferenceRegex
            .findAll(
                file.readText()
            )
            .map { match ->
                match.groupValues[1]
            }
            .filter { reference ->
                reference.isNotBlank()
            }
            .toSet()
    }
}