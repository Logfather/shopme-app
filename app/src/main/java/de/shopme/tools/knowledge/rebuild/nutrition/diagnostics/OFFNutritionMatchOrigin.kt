package de.shopme.tools.knowledge.rebuild.nutrition.diagnostics

enum class OFFNutritionMatchOrigin {
    DIRECT_PRODUCT_IDENTITY,
    INGREDIENT_ONLY,
}

data class OFFNutritionIdentityMatch(
    val origin: OFFNutritionMatchOrigin,
    val matchedAliases: List<String>,
)

class OFFNutritionMatchOriginClassifier {

    fun classify(
        aliases: Collection<String>,
        directIdentityValues: Collection<String>,
        ingredientIdentityValues: Collection<String>,
    ): OFFNutritionIdentityMatch? {

        val normalizedAliases =
            aliases
                .map(::normalize)
                .filter(String::isNotBlank)
                .distinct()
                .sorted()

        val normalizedDirectValues =
            directIdentityValues
                .map(::normalize)
                .filter(String::isNotBlank)
                .distinct()
                .sorted()

        val directAliases =
            normalizedAliases.filter { alias ->
                normalizedDirectValues.any { value ->
                    containsAlias(
                        value =
                            value,
                        alias =
                            alias
                    )
                }
            }

        if (directAliases.isNotEmpty()) {
            return OFFNutritionIdentityMatch(
                origin =
                    OFFNutritionMatchOrigin
                        .DIRECT_PRODUCT_IDENTITY,
                matchedAliases =
                    directAliases
            )
        }

        val normalizedIngredientValues =
            ingredientIdentityValues
                .map(::normalize)
                .filter(String::isNotBlank)
                .distinct()
                .sorted()

        val ingredientAliases =
            normalizedAliases.filter { alias ->
                normalizedIngredientValues.any { value ->
                    containsAlias(
                        value =
                            value,
                        alias =
                            alias
                    )
                }
            }

        if (ingredientAliases.isEmpty()) {
            return null
        }

        return OFFNutritionIdentityMatch(
            origin =
                OFFNutritionMatchOrigin
                    .INGREDIENT_ONLY,
            matchedAliases =
                ingredientAliases
        )
    }

    private fun containsAlias(
        value: String,
        alias: String,
    ): Boolean {

        val valueTokens =
            value.split(
                WHITESPACE_REGEX
            )

        val aliasTokens =
            alias.split(
                WHITESPACE_REGEX
            )

        if (
            aliasTokens.isEmpty() ||
            aliasTokens.size >
            valueTokens.size
        ) {
            return false
        }

        return valueTokens
            .windowed(
                size =
                    aliasTokens.size
            )
            .any { tokens ->
                tokens ==
                        aliasTokens
            }
    }

    private fun normalize(
        value: String,
    ): String {

        return java.text.Normalizer
            .normalize(
                value.lowercase(),
                java.text.Normalizer.Form.NFKD
            )
            .replace(
                Regex("\\p{M}+"),
                ""
            )
            .replace(
                Regex("[^a-z0-9]+"),
                " "
            )
            .trim()
            .replace(
                WHITESPACE_REGEX,
                " "
            )
    }

    private companion object {

        val WHITESPACE_REGEX =
            Regex("\\s+")
    }
}