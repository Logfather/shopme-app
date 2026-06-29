package de.shopme.tools.knowledge.off

class CarbonBaseFoodVariantResolver {

    fun resolve(
        normalizedName: String,
        coveredCarbonNames: Set<String>
    ): String? {

        val normalized =
            normalizedName
                .trim()
                .lowercase()

        if (normalized in coveredCarbonNames) {
            return normalized
        }

        val tokens =
            normalized
                .split(" ")
                .filter {
                    it.isNotBlank()
                }

        val cleanedTokens =
            tokens
                .filterNot {
                    it in removableModifiers
                }

        val isPreparedMeal =
            tokens.any {
                it in preparedMealTokens
            }

        val cleanedName =
            cleanedTokens
                .joinToString(" ")

        if (cleanedName in coveredCarbonNames) {
            return cleanedName
        }

        val compactName =
            cleanedTokens
                .joinToString("")

        if (compactName in coveredCarbonNames) {
            return compactName
        }

        if (isPreparedMeal) {
            return null
        }

        return findTokenMatch(
            tokens = cleanedTokens,
            coveredCarbonNames = coveredCarbonNames
        )
    }

    private fun findTokenMatch(
        tokens: List<String>,
        coveredCarbonNames: Set<String>
    ): String? {

        return tokens
            .filter {
                it.length >= 4
            }
            .filterNot {
                it in forbiddenBaseTokens
            }
            .firstOrNull {
                it in coveredCarbonNames
            }
    }

    private companion object {

        val removableModifiers =
            setOf(
                "bio",
                "standard",
                "frisch",
                "frische",
                "frischer",
                "frisches",
                "tk",
                "tiefkuehl",
                "tiefgekuehlt",
                "gefroren",
                "dose",
                "dosen",
                "konserve",
                "konserviert",
                "in",
                "aus",
                "mit",
                "ohne",
                "natur",
                "mild",
                "klassisch",
                "weiss",
                "rot",
                "gruen",
                "schwarz",
                "gelb",
                "braun",
                "grob",
                "fein",
                "gemahlen",
                "getrocknet",
                "geraechert",
                "geraeuchert"
            )

        val forbiddenBaseTokens =
            setOf(
                "wein",
                "oel",
                "sauce",
                "sosse",
                "gewuerz",
                "pulver",
                "fertiggericht",
                "standard",
                "bio",
                "frisch",
                "tiefkuehl",
                "tiefgekuehlt",
                "konserve"
            )
    }

    val preparedMealTokens =
        setOf(
            "fertiggericht",
            "pfanne",
            "auflauf",
            "eintopf",
            "gericht",
            "bolognese",
            "con",
            "carne"
        )
}