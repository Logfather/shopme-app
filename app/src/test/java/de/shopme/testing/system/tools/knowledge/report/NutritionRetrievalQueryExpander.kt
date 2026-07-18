package de.shopme.testing.system.tools.knowledge.report

import java.text.Normalizer
import java.util.Locale

class NutritionRetrievalQueryExpander(
    private val aliasRegistry:
    NutritionRetrievalAliasRegistry =
        NutritionRetrievalAliasRegistry()
) {

    fun expand(
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

        val aliases =
            aliasRegistry
                .aliasesFor(
                    catalogKey =
                        normalizedCatalogKey
                )
                .asSequence()
                .map(
                    ::normalize
                )
                .filter {
                    it.isNotBlank()
                }
                .filterNot {
                    it ==
                            normalizedCatalogKey
                }
                .distinct()
                .sorted()
                .toList()

        /*
         * Die Originalform steht immer an erster Stelle.
         *
         * Bei identischen Scores gewinnt dadurch deterministisch
         * das Retrieval über den ursprünglichen Catalog Key.
         */
        return listOf(
            normalizedCatalogKey
        ) +
                aliases
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

    private companion object {

        val DIACRITIC_REGEX =
            Regex("\\p{M}+")

        val NON_ALPHANUMERIC_REGEX =
            Regex("[^\\p{L}\\p{N} ]+")

        val WHITESPACE_REGEX =
            Regex("\\s+")
    }
}