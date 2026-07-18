package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

import java.text.Normalizer
import java.util.Locale

class FoodDomainTokenNormalizer {

    fun normalize(
        token: String,
    ): String {
        val normalized =
            Normalizer
                .normalize(
                    token,
                    Normalizer.Form.NFKD,
                )
                .replace(
                    combiningMarkRegex,
                    "",
                )
                .lowercase(Locale.ROOT)
                .trim()
                .replace(
                    nonAlphaNumericRegex,
                    "",
                )

        return singularize(
            token = normalized,
        )
    }

    private fun singularize(
        token: String,
    ): String =
        irregularSingulars[token]
            ?: when {
                token.endsWith("ies") &&
                        token.length > 3 ->
                    token.dropLast(3) + "y"

                token.endsWith("ves") &&
                        token.length > 3 ->
                    token.dropLast(3) + "f"

                token.endsWith("oes") &&
                        token.length > 3 ->
                    token.dropLast(2)

                token.endsWith("ches") &&
                        token.length > 4 ->
                    token.dropLast(2)

                token.endsWith("shes") &&
                        token.length > 4 ->
                    token.dropLast(2)

                token.endsWith("xes") &&
                        token.length > 3 ->
                    token.dropLast(2)

                token.endsWith("zes") &&
                        token.length > 3 ->
                    token.dropLast(2)

                token.endsWith("ses") &&
                        token.length > 3 ->
                    token.dropLast(2)

                token.endsWith("s") &&
                        token.length > 2 &&
                        !token.endsWith("ss") ->
                    token.dropLast(1)

                else ->
                    token
            }

    private companion object {

        val combiningMarkRegex =
            Regex("\\p{M}+")

        val nonAlphaNumericRegex =
            Regex("[^a-z0-9]+")

        val irregularSingulars =
            mapOf(
                "fries" to "fry",
                "mangoes" to "mango",
                "potatoes" to "potato",
                "tomatoes" to "tomato",
                "leaves" to "leaf",
                "loaves" to "loaf",
                "wives" to "wife",
            )
    }
}