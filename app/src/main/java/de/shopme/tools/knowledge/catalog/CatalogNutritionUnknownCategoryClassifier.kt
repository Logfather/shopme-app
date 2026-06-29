package de.shopme.tools.knowledge.catalog

class CatalogNutritionUnknownCategoryClassifier {

    fun classify(
        name: String
    ): CatalogNutritionUnknownCategory {

        fun tokensOf(
            value: String
        ): Set<String> {

            return value
                .split(" ")
                .filter {
                    it.isNotBlank()
                }
                .toSet()

        }

        val value =
            name.lowercase()

        val tokens =
            tokensOf(value)

        if (
            tokens.contains("saft") ||
            value.endsWith("saft") ||
            tokens.contains("wasser") ||
            value.endsWith("wasser") ||
            tokens.contains("cola") ||
            value.endsWith("cola") ||
            tokens.contains("bier") ||
            tokens.contains("wein") ||
            tokens.contains("tee") ||
            value.endsWith("tee") ||
            tokens.contains("limonade") ||
            value.endsWith("limonade") ||
            tokens.contains("drink") ||
            value.endsWith("drink")
        ) {
            return CatalogNutritionUnknownCategory.BEVERAGES
        }

        if (
            value.contains("brot") ||
            value.contains("broetchen") ||
            value.contains("baguette") ||
            value.contains("brezel") ||
            value.contains("croissant")
        ) {
            return CatalogNutritionUnknownCategory.BAKERY
        }

        if (
            value.contains("kaese") ||
            value.contains("joghurt") ||
            value.contains("quark") ||
            value.contains("mozzarella") ||
            value.contains("camembert") ||
            value.contains("gouda") ||
            value.contains("feta") ||
            value.contains("milch")
        ) {
            return CatalogNutritionUnknownCategory.DAIRY
        }

        if (
            value.contains("schokolade") ||
            value.contains("keks") ||
            value.contains("bonbon") ||
            value.contains("praline") ||
            value.contains("marmelade") ||
            value.contains("nougat")
        ) {
            return CatalogNutritionUnknownCategory.SWEETS
        }

        if (
            value.contains("chips") ||
            value.contains("cracker") ||
            value.contains("waffel") ||
            value.contains("riegel")
        ) {
            return CatalogNutritionUnknownCategory.SNACKS
        }

        return when (name) {

            "pizza",
            "pizza salami",
            "gemuesepfanne",
            "chili",
            "currywurst",
            "gulasch",
            "curry",
            "kartoffelgratin",
            "erbsensuppe",
            "eintopf",
            "sauerkraut",
            "gemueselasagne",
            "spaghetti bolognese",
            "moussaka" -> CatalogNutritionUnknownCategory.DISHES

            "camembert" -> CatalogNutritionUnknownCategory.CHEESE

            "couscous",
            "gerste",
            "kamut",
            "gruenkern",
            "dinkelflocken",
            "haferkleie" -> CatalogNutritionUnknownCategory.GRAINS

            "anis",
            "piment",
            "kraeuter der provence",
            "edelsuess" -> CatalogNutritionUnknownCategory.SPICES

            "remoulade",
            "salatdressing french",
            "dressing joghurt kraeuter" -> CatalogNutritionUnknownCategory.SAUCES

            "kuerbiskernoel",
            "disteloel" -> CatalogNutritionUnknownCategory.OILS

            "hering in tomatensauce" -> CatalogNutritionUnknownCategory.FISH

            "schweinehack",
            "schnitzel" -> CatalogNutritionUnknownCategory.MEAT

            "edamame" -> CatalogNutritionUnknownCategory.VEGETABLES

            "physalis",
            "avocado" -> CatalogNutritionUnknownCategory.FRUITS

            else -> CatalogNutritionUnknownCategory.OTHER

        }

    }

}