package de.shopme.tools.knowledge.off

import java.io.File

class OFFProductCandidateLoader(
    private val reader: OFFJsonlGzipReader = OFFJsonlGzipReader(),
    private val extractor: OFFMinimalProductSourceExtractor = OFFMinimalProductSourceExtractor()
) {

    fun load(
        input: File
    ): List<OFFProductCandidate> {

        if (!input.exists()) {
            return emptyList()
        }

        return reader
            .read(input)
            .map(extractor::extract)
            .mapNotNull(::mapProductCandidate)
    }

    private fun mapProductCandidate(
        source: OFFMinimalProductSource
    ): OFFProductCandidate? {

        val productName =
            source.productName
                ?.takeIf(String::isNotBlank)
                ?: source.genericName
                    ?.takeIf(String::isNotBlank)
                ?: return null

        return OFFProductCandidate(
            id = source.code,
            productName = productName,
            normalizedName = normalize(productName),
            hasNutritionFacts = hasNutritionFacts(source),
            hasAllergens = source.allergensTags.isNotEmpty()
        )
    }

    private fun hasNutritionFacts(
        source: OFFMinimalProductSource
    ): Boolean {

        val nutrimentsJson =
            source.nutrimentsJson
                ?: return false

        return nutrimentsJson.contains("\"energy-kcal_100g\"") ||
                nutrimentsJson.contains("\"energy_100g\"") ||
                nutrimentsJson.contains("\"proteins_100g\"") ||
                nutrimentsJson.contains("\"fat_100g\"") ||
                nutrimentsJson.contains("\"carbohydrates_100g\"")
    }

    private fun normalize(
        value: String
    ): String {

        return value
            .lowercase()
            .trim()
            .replace("ä", "ae")
            .replace("ö", "oe")
            .replace("ü", "ue")
            .replace("ß", "ss")
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
    }
}