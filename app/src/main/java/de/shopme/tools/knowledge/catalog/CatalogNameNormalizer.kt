package de.shopme.tools.knowledge.catalog

class CatalogNameNormalizer {

    private fun normalizeBakeryVariants(
        value: String
    ): String {

        return when {

            value.contains("brot") ->
                "bread"

            value.contains("broetchen") ->
                "bread"

            value.contains("baguette") ->
                "bread"

            value.contains("toast") ->
                "bread"

            value.contains("ciabatta") ->
                "bread"

            value.contains("knaeckebrot") ->
                "bread"

            value.contains("pita") ->
                "bread"

            value.contains("croissant") ->
                "bread"

            value.contains("brezel") ->
                "bread"

            else ->
                value
        }

    }

    fun normalize(
        value: String
    ): String {

        fun normalizeOilVariants(
            value: String
        ): String {

            return when {

                value.contains("oel") ->
                    "oil"

                else ->
                    value
            }

        }

        fun normalizeWaterVariants(
            value: String
        ): String {

            return when {

                value.contains("mineralwasser") ->
                    "water"

                value.contains("tafelwasser") ->
                    "water"

                value == "wasser still" ->
                    "water"

                value == "wasser mit kohlensaeure" ->
                    "water"

                value.contains("quelle mineralwasser") ->
                    "water"

                else ->
                    value
            }

        }

        fun normalizeCannedVegetableVariants(
            value: String
        ): String {

            return when (value) {

                "maiskoerner" -> "mais"
                "zuckermais" -> "mais"

                "gehackte tomaten" -> "tomate"
                "tomatenstueckchen" -> "tomate"
                "tomatenstuecken" -> "tomate"
                "tomaten geschaelt" -> "tomate"
                "tomaten gestueckelt" -> "tomate"

                "pfirsichhaelften" -> "pfirsich"

                "apfelmus" -> "apple"

                else -> value
            }

        }

        fun normalizePluralFoodNames(
            value: String
        ): String {

            return when (value) {

                "mangos" -> "mango"
                "auberginen" -> "aubergine"
                "avocados" -> "avocado"
                "limetten" -> "limette"
                "kiwis" -> "kiwi"
                "melonen" -> "melone"
                "mandarinen" -> "mandarine"

                else -> value
            }

        }

        fun String.normalizePreparedDishVariant(): String {

            return when {

                contains("auflauf") ->
                    "auflauf"

                contains("gratin") ->
                    "gratin"

                contains("pfannengericht") ->
                    "gemuesepfanne"

                contains("gemuesepfanne") ->
                    "gemuesepfanne"

                contains("bratkartoffeln") ->
                    "bratkartoffeln"

                contains("bolognese") ->
                    "bolognese"

                contains("chili") ->
                    "chili"

                contains("pizza") ->
                    "pizza"

                contains("lasagne") ->
                    "lasagne"

                contains("eintopf") ->
                    "eintopf"

                contains("gulasch") ->
                    "gulasch"

                contains("currywurst") ->
                    "currywurst"

                contains("curry") ->
                    "curry"

                contains("suppe") ->
                    "suppe"

                contains("ratatouille") ->
                    "ratatouille"

                contains("moussaka") ->
                    "moussaka"

                else ->
                    this

            }

        }

        fun String.cleanupCatalogNutritionTokens(): String {

            val phraseCleaned =
                this
                    .replace(" in dose", "")
                    .replace(" in dosen", "")
                    .replace(" aus der dose", "")
                    .replace(" aus dosen", "")
                    .replace(" tiefgekuehlt", "")
                    .replace(" tiefgefroren", "")
                    .replace("tiefkuehl", "")
                    .replace("tk", "")

            val removableTokens =
                setOf(
                    "frisch",
                    "frische",
                    "frischer",
                    "frisches",
                    "gefroren",
                    "gefrorene",
                    "gefrorener",
                    "gefrorenes",
                    "tiefgekuehlt",
                    "tiefgekuehlte",
                    "tiefgekuehlter",
                    "tiefgekuehltes",
                    "konserve",
                    "konserviert",
                    "dose",
                    "dosen"
                )

            return phraseCleaned
                .split(" ")
                .filter {
                    it.isNotBlank()
                }
                .filterNot {
                    it in removableTokens
                }
                .joinToString(" ")

        }

        return value
            .lowercase()
            .trim()
            .removeSuffix(" bio")
            .removeSuffix(" standard")
            .removePrefix("tiefkuehl ")
            .removePrefix("tk ")
            .removePrefix("fertiggericht ")
            .removePrefix("vegetarische ")
            .removePrefix("vegetarischer ")
            .removePrefix("vegetarisches ")
            .removePrefix("vegane ")
            .removePrefix("veganer ")
            .removePrefix("veganes ")
            .removeSuffix(" rot")
            .removeSuffix(" gruen")
            .removeSuffix(" geraeuchert")
            .removeSuffix(" gekocht")
            .removeSuffix(" roh")
            .removeSuffix(" naturtrueb")
            .removeSuffix(" medium")
            .removeSuffix(" fein")
            .removeSuffix(" gemahlen")
            .removeSuffix(" tiefgekuehlt")
            .removeSuffix(" tiefkuehl")
            .removeSuffix(" tk")
            .removeSuffix(" gesalzen")
            .removeSuffix(" edelsuess")
            .removeSuffix(" ganz")
            .removeSuffix(" natur")
            .removeSuffix(" nativ extra")
            .removeSuffix(" extra vergine")
            .removeSuffix(" vanille")
            .removeSuffix(" erdbeere")
            .removeSuffix(" 70")
            .removePrefix("passierte ")
            .removePrefix("getrocknete ")
            .cleanupCatalogNutritionTokens()
            .let(::normalizeCannedVegetableVariants)
            .let(::normalizePluralFoodNames)
            .let(::normalizeBakeryVariants)
            .let(::normalizeOilVariants)
            .let(::normalizeWaterVariants)
            .normalizePreparedDishVariant()
            .trim()
    }
}