package de.shopme.domain.food

class LocalFoodClassifier(

    private val foodKnowledge:
    FoodKnowledgeLookup

) : FoodClassifier {

    override fun classify(
        productName: String
    ): FoodCategory {

        val normalized =
            productName
                .lowercase()
                .trim()

        foodKnowledge.find(normalized)?.let {

            return it.category

        }

        return when {

            fruitKeywords.any(normalized::contains) ->
                FoodCategory.FRUIT

            vegetableKeywords.any(normalized::contains) ->
                FoodCategory.VEGETABLE

            proteinKeywords.any(normalized::contains) ->
                FoodCategory.PROTEIN

            processedKeywords.any(normalized::contains) ->
                FoodCategory.PROCESSED

            dairyKeywords.any(normalized::contains) ->
                FoodCategory.DAIRY

            grainKeywords.any(normalized::contains) ->
                FoodCategory.GRAIN

            beverageKeywords.any(normalized::contains) ->
                FoodCategory.BEVERAGE

            nutKeywords.any(normalized::contains) ->
                FoodCategory.NUTS

            legumeKeywords.any(normalized::contains) ->
                FoodCategory.LEGUMES

            else ->
                FoodCategory.UNKNOWN

        }

    }

    companion object {

        private val fruitKeywords = setOf(
            "apfel",
            "banane",
            "birne",
            "orange",
            "kiwi",
            "erdbeere",
            "himbeere",
            "heidelbeere",
            "ananas",
            "mango"
        )

        private val vegetableKeywords = setOf(
            "tomate",
            "gurke",
            "paprika",
            "salat",
            "brokkoli",
            "spinat",
            "karotte",
            "zwiebel",
            "zucchini"
        )

        private val proteinKeywords = setOf(
            "ei",
            "eier",
            "quark",
            "skyr",
            "tofu",
            "hähnchen",
            "lachs",
            "thunfisch",
            "bohnen",
            "linsen"
        )

        private val processedKeywords = setOf(
            "cola",
            "chips",
            "pizza",
            "nutella",
            "kekse",
            "schokolade",
            "energy",
            "limonade"
        )

        private val dairyKeywords = setOf(
            "milch",
            "käse",
            "joghurt",
            "butter",
            "sahne"
        )

        private val grainKeywords = setOf(
            "brot",
            "reis",
            "nudeln",
            "hafer",
            "müsli"
        )

        private val beverageKeywords = setOf(
            "wasser",
            "saft",
            "tee",
            "kaffee"
        )

        private val nutKeywords = setOf(
            "mandel",
            "walnuss",
            "haselnuss"
        )

        private val legumeKeywords = setOf(
            "linse",
            "bohne",
            "erbse",
            "kichererbse"
        )

    }

}