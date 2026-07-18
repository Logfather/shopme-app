package de.shopme.testing.system.tools.knowledge.report

import java.text.Normalizer
import java.util.Locale

class NutritionRetrievalAliasRegistry(
    aliases: Map<String, Set<String>> =
        DEFAULT_ALIASES
) {

    private val aliasesByNormalizedKey:
            Map<String, List<String>> =
        buildAliasIndex(
            aliases =
                aliases
        )

    fun aliasesFor(
        catalogKey: String
    ): List<String> {

        val normalizedCatalogKey =
            normalize(
                value =
                    catalogKey
            )

        if (normalizedCatalogKey.isBlank()) {
            return emptyList()
        }

        return aliasesByNormalizedKey[
            normalizedCatalogKey
        ]
            .orEmpty()
    }

    private fun buildAliasIndex(
        aliases: Map<String, Set<String>>
    ): Map<String, List<String>> {

        val mutableAliases =
            sortedMapOf<String, MutableSet<String>>()

        aliases.forEach { (rawKey, rawAliases) ->

            val normalizedKey =
                normalize(
                    value =
                        rawKey
                )

            require(normalizedKey.isNotBlank()) {
                "Nutrition retrieval alias key must not be blank."
            }

            rawAliases.forEach aliasLoop@{ rawAlias ->

                val normalizedAlias =
                    normalize(
                        value =
                            rawAlias
                    )

                require(normalizedAlias.isNotBlank()) {
                    "Nutrition retrieval alias must not be blank for " +
                            "key '$normalizedKey'."
                }

                if (
                    normalizedAlias ==
                    normalizedKey
                ) {
                    return@aliasLoop
                }

                mutableAliases
                    .getOrPut(
                        normalizedKey
                    ) {
                        sortedSetOf()
                    }
                    .add(
                        normalizedAlias
                    )
            }
        }

        return mutableAliases
            .mapValues { (_, values) ->

                values
                    .distinct()
                    .sorted()
            }
            .toSortedMap()
    }

    private fun normalize(
        value: String
    ): String {

        val germanTransliterated =
            value
                .lowercase(
                    Locale.ROOT
                )
                .replace(
                    "ä",
                    "ae"
                )
                .replace(
                    "ö",
                    "oe"
                )
                .replace(
                    "ü",
                    "ue"
                )
                .replace(
                    "ß",
                    "ss"
                )

        val decomposed =
            Normalizer.normalize(
                germanTransliterated,
                Normalizer.Form.NFKD
            )

        return decomposed
            .replace(
                DIACRITIC_REGEX,
                ""
            )
            .replace(
                "-",
                " "
            )
            .replace(
                "_",
                " "
            )
            .replace(
                NON_ALPHANUMERIC_REGEX,
                " "
            )
            .replace(
                WHITESPACE_REGEX,
                " "
            )
            .trim()
    }

    companion object {

        val DEFAULT_ALIASES:
                Map<String, Set<String>> =
            linkedMapOf(
                "chervil" to
                        setOf(
                            "kerbel"
                        ),
                "leberkaese" to
                        setOf(
                            "leberkäse",
                            "leberkase",
                            "bavarian meat loaf",
                            "bavarian meatloaf",
                            "meat loaf",
                            "meatloaf"
                        ),
                "mace" to
                        setOf(
                            "muskatblüte",
                            "muskatbluete",
                            "mace spice"
                        ),
                "salsify" to
                        setOf(
                            "schwarzwurzel",
                            "black salsify"
                        ),
                "teewurst" to
                        setOf(
                            "tea sausage",
                            "spreadable sausage",
                            "smoked spreadable sausage"
                        ),
                "toffifee" to
                        setOf(
                            "caramel hazelnut confectionery",
                            "chocolate caramel hazelnut candy",
                            "hazelnut caramel candy"
                        )
            )

        private val DIACRITIC_REGEX =
            Regex("\\p{M}+")

        private val NON_ALPHANUMERIC_REGEX =
            Regex("[^\\p{L}\\p{N} ]+")

        private val WHITESPACE_REGEX =
            Regex("\\s+")
    }
}