package de.shopme.tools.knowledge.agribalyse.mapper

import de.shopme.tools.knowledge.agribalyse.model.AgribalyseMappingResult
import java.text.Normalizer

class AgribalyseReferenceMapper(
    mappings: Map<String, String> = defaultMappings
) {

    private val normalizedMappings =
        mappings.mapKeys { (key, _) ->
            normalize(key)
        }

    fun map(
        reference: String
    ): AgribalyseMappingResult {

        val normalizedReference =
            normalize(reference)

        val mappedReference =
            normalizedMappings[normalizedReference]

        return if (mappedReference != null) {
            AgribalyseMappingResult(
                reference = mappedReference,
                mapped = true
            )
        } else {
            AgribalyseMappingResult(
                reference = normalizedReference,
                mapped = false
            )
        }
    }

    private fun normalize(
        value: String
    ): String {

        val withoutAccents =
            Normalizer.normalize(
                value,
                Normalizer.Form.NFD
            ).replace(
                "\\p{Mn}+".toRegex(),
                ""
            )

        return withoutAccents
            .lowercase()
            .trim()
            .replace(
                "\\s+".toRegex(),
                " "
            )
    }

    fun mappedCount(): Int =
        normalizedMappings.size

    companion object {

        private val defaultMappings =
            mapOf(
                // Apple
                "pomme, pulpe et peau, crue" to "apple",
                "pomme, avec peau, crue" to "apple",

                // Tomato
                "tomate cerise, crue" to "tomato",
                "tomate ronde, crue" to "tomato",

                // Rice
                "riz complet, cuit" to "rice",
                "riz blanc étuvé, cuit" to "rice",

                // Potato
                "pomme de terre vapeur" to "potato",
                "pomme de terre au four" to "potato",

                // Carrot
                "carotte râpée, crue" to "carrot",

                // Broccoli
                "brocoli cuit" to "broccoli",

                // Spinach
                "épinard cuit" to "spinach",

                // Milk
                "lait écrémé, uht" to "milk",
                "lait entier, pasteurisé" to "milk",

                // Bread
                "pain complet" to "bread",
                "pain de mie" to "bread",

                // Pasta
                "pâtes alimentaires, au blé complet, cuites" to "pasta",
                "spaghetti, cuits" to "pasta",

                // Lentils
                "lentilles vertes, cuites" to "lentils",

                // Coffee
                "café noir, sans sucre" to "coffee",

                // Tea
                "thé vert, infusion" to "tea",
                "thé noir, infusion" to "tea",

                // Water
                "eau minérale" to "water",

                // Salmon
                "saumon fumé" to "salmon",

                // Tofu
                "tofu ferme" to "tofu",
                "tofu fumé" to "tofu"
            )
    }
}