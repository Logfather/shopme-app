package de.shopme.tools.knowledge.off

class OFFMinimalProductSourceExtractor {

    fun extract(
        rawJsonLine: String
    ): OFFMinimalProductSource {

        return OFFMinimalProductSource(
            code = extractString(rawJsonLine, "code"),
            productName = extractString(rawJsonLine, "product_name"),
            genericName = extractString(rawJsonLine, "generic_name"),
            brands = extractString(rawJsonLine, "brands"),
            categoriesTags = extractStringArray(rawJsonLine, "categories_tags"),
            ingredientsText = extractString(rawJsonLine, "ingredients_text"),
            allergensTags = extractStringArray(rawJsonLine, "allergens_tags"),
            nutrimentsJson = extractObjectJson(rawJsonLine, "nutriments"),
            novaGroup = extractNumberOrString(rawJsonLine, "nova_group"),
            ecoScoreGrade = extractString(rawJsonLine, "ecoscore_grade"),
            labelsTags = extractStringArray(rawJsonLine, "labels_tags"),
            countriesTags = extractStringArray(rawJsonLine, "countries_tags")
        )
    }

    private fun extractString(
        json: String,
        key: String
    ): String? {

        val regex =
            """"$key"\s*:\s*"((?:\\.|[^"\\])*)"""".toRegex()

        return regex
            .find(json)
            ?.groupValues
            ?.get(1)
            ?.takeIf(String::isNotBlank)
    }

    private fun extractNumberOrString(
        json: String,
        key: String
    ): String? {

        val stringValue =
            extractString(json, key)

        if (stringValue != null) {
            return stringValue
        }

        val regex =
            """"$key"\s*:\s*([0-9]+)""".toRegex()

        return regex
            .find(json)
            ?.groupValues
            ?.get(1)
    }

    private fun extractStringArray(
        json: String,
        key: String
    ): List<String> {

        val arrayContentRegex =
            """"$key"\s*:\s*\[(.*?)]""".toRegex()

        val content =
            arrayContentRegex
                .find(json)
                ?.groupValues
                ?.get(1)
                ?: return emptyList()

        val itemRegex =
            """"((?:\\.|[^"\\])*)"""".toRegex()

        return itemRegex
            .findAll(content)
            .map { match ->
                match.groupValues[1]
            }
            .filter(String::isNotBlank)
            .toList()
    }

    private fun extractObjectJson(
        json: String,
        key: String
    ): String? {

        val keyIndex =
            json.indexOf("\"$key\"")

        if (keyIndex == -1) {
            return null
        }

        val objectStart =
            json.indexOf('{', keyIndex)

        if (objectStart == -1) {
            return null
        }

        var depth = 0

        for (index in objectStart until json.length) {

            when (json[index]) {
                '{' -> depth++
                '}' -> {
                    depth--

                    if (depth == 0) {
                        return json.substring(
                            objectStart,
                            index + 1
                        )
                    }
                }
            }
        }

        return null
    }
}