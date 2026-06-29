package de.shopme.tools.knowledge.catalog

import java.io.File

class CatalogNutritionReferenceNormalizer(
    private val aliasMapper: CatalogNutritionReferenceParser =
        CatalogNutritionReferenceParser()
) {

    fun normalize(
        inputFile: File,
        outputFile: File
    ) {

        val normalizedObjects =
            splitTopLevelObjects(
                inputFile.readText()
            ).map { objectJson ->

                normalizeObject(
                    objectJson
                )
            }

        outputFile.writeText(
            normalizedObjects.joinToString(
                separator = ",\n",
                prefix = "[\n",
                postfix = "\n]"
            )
        )
    }

    private fun normalizeObject(
        objectJson: String
    ): String {

        val normalizedName =
            Regex(
                """"normalized"\s*:\s*"([^"]+)""""
            )
                .find(
                    objectJson
                )
                ?.groupValues
                ?.get(1)

        val legacyReference =
            Regex(
                """"nutritionReference"\s*:\s*"([^"]+)""""
            )
                .find(
                    objectJson
                )
                ?.groupValues
                ?.get(1)

        val existingNutritionReference =
            Regex(
                """"nutrition"\s*:\s*\{[\s\S]*?"reference"\s*:\s*"([^"]+)""""
            )
                .find(
                    objectJson
                )
                ?.groupValues
                ?.get(1)

        val reference =
            when {
                existingNutritionReference != null ->
                    aliasMapper.map(existingNutritionReference)

                legacyReference != null ->
                    aliasMapper.map(legacyReference)

                normalizedName != null &&
                        aliasMapper.hasAlias(normalizedName) ->
                    aliasMapper.map(normalizedName)

                else ->
                    "unknown"
            }

        val source =
            if (reference == "unknown") {
                "unknown"
            } else {
                "open_food_facts"
            }

        val withoutLegacy =
            objectJson
                .replace(
                    Regex(
                        """,?\s*"nutritionReference"\s*:\s*"[^"]+""""
                    ),
                    ""
                )

        return if (withoutLegacy.contains(""""knowledge"""")) {

            if (withoutLegacy.contains(""""nutrition"""")) {

                withoutLegacy.replace(
                    Regex(
                        """"nutrition"\s*:\s*\{[\s\S]*?\}"""
                    ),
                    """
                    "nutrition": {
                      "reference": "$reference",
                      "source": "$source"
                    }
                    """.trimIndent()
                )

            } else {

                withoutLegacy.replace(
                    Regex(
                        """"knowledge"\s*:\s*\{"""
                    ),
                    """
                    "knowledge": {
                      "nutrition": {
                        "reference": "$reference",
                        "source": "$source"
                      },
                    """.trimIndent()
                )
            }

        } else {

            withoutLegacy.replace(
                Regex(
                    """"autocomplete_tokens"\s*:\s*\[[\s\S]*?]"""
                )
            ) { match ->

                """
                ${match.value},
                "knowledge": {
                  "nutrition": {
                    "reference": "$reference",
                    "source": "$source"
                  }
                }
                """.trimIndent()
            }
        }
    }

    private fun splitTopLevelObjects(
        json: String
    ): List<String> {

        val objects =
            mutableListOf<String>()

        var depth = 0
        var startIndex = -1
        var insideString = false
        var escaped = false

        json.forEachIndexed { index, char ->

            if (escaped) {
                escaped = false
                return@forEachIndexed
            }

            if (char == '\\' && insideString) {
                escaped = true
                return@forEachIndexed
            }

            if (char == '"') {
                insideString = !insideString
                return@forEachIndexed
            }

            if (insideString) {
                return@forEachIndexed
            }

            when (char) {

                '{' -> {
                    if (depth == 0) {
                        startIndex = index
                    }

                    depth++
                }

                '}' -> {
                    depth--

                    if (depth == 0 && startIndex >= 0) {
                        objects.add(
                            json.substring(
                                startIndex,
                                index + 1
                            )
                        )

                        startIndex = -1
                    }
                }
            }
        }

        return objects
    }
}