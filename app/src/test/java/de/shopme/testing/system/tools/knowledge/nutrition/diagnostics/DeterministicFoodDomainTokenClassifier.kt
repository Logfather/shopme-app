package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

class DeterministicFoodDomainTokenClassifier(
    private val normalizer: FoodDomainTokenNormalizer =
        FoodDomainTokenNormalizer(),
) {

    init {
        validateVocabularyDisjointness()
    }

    fun classify(
        token: String,
    ): FoodDomainTokenClassification {

        val normalizedToken =
            normalizer.normalize(
                token = token,
            )

        val tokenClass =
            determineTokenClass(
                normalizedToken = normalizedToken,
            )

        return FoodDomainTokenClassification(
            originalToken = token,
            normalizedToken = normalizedToken,
            tokenClass = tokenClass,
        )
    }

    fun classifyNormalized(
        normalizedToken: String,
    ): FoodDomainTokenClass {
        require(normalizedToken == normalizer.normalize(normalizedToken)) {
            "Expected an already normalized token: $normalizedToken"
        }

        return determineTokenClass(
            normalizedToken = normalizedToken,
        )
    }

    private fun determineTokenClass(
        normalizedToken: String,
    ): FoodDomainTokenClass =
        when {
            normalizedToken.isEmpty() ->
                FoodDomainTokenClass.UNKNOWN

            normalizedToken.all(Char::isDigit) ->
                FoodDomainTokenClass.NUMERIC

            normalizedToken in stopwords ->
                FoodDomainTokenClass.STOPWORD

            normalizedToken in animalSpecies ->
                FoodDomainTokenClass.ANIMAL_SPECIES

            normalizedToken in animalProductsOrCuts ->
                FoodDomainTokenClass.ANIMAL_PRODUCT_OR_CUT

            normalizedToken in processedAnimalProducts ->
                FoodDomainTokenClass.PROCESSED_ANIMAL_PRODUCT

            normalizedToken in grainsOrLegumes ->
                FoodDomainTokenClass.GRAIN_OR_LEGUME

            normalizedToken in nutsSeedsOrOilSources ->
                FoodDomainTokenClass.NUT_SEED_OR_OIL_SOURCE

            normalizedToken in herbsOrSpices ->
                FoodDomainTokenClass.HERB_OR_SPICE

            normalizedToken in plantIngredients ->
                FoodDomainTokenClass.PLANT_INGREDIENT

            normalizedToken in dairyProducts ->
                FoodDomainTokenClass.DAIRY_PRODUCT

            normalizedToken in dishesOrMeals ->
                FoodDomainTokenClass.DISH_OR_MEAL

            normalizedToken in bakeryOrStarchProducts ->
                FoodDomainTokenClass.BAKERY_OR_STARCH_PRODUCT

            normalizedToken in beverages ->
                FoodDomainTokenClass.BEVERAGE

            normalizedToken in sweetProducts ->
                FoodDomainTokenClass.SWEET_PRODUCT

            normalizedToken in productForms ->
                FoodDomainTokenClass.PRODUCT_FORM

            normalizedToken in preparationOrProcessing ->
                FoodDomainTokenClass.PREPARATION_OR_PROCESSING

            normalizedToken in colorsOrAppearance ->
                FoodDomainTokenClass.COLOR_OR_APPEARANCE

            normalizedToken in regionsOrCuisines ->
                FoodDomainTokenClass.REGION_OR_CUISINE

            normalizedToken in styleOrQualityModifiers ->
                FoodDomainTokenClass.STYLE_OR_QUALITY_MODIFIER

            normalizedToken in quantityOrSizeModifiers ->
                FoodDomainTokenClass.QUANTITY_OR_SIZE_MODIFIER

            normalizedToken in dietOrSubstitutes ->
                FoodDomainTokenClass.DIET_OR_SUBSTITUTE

            normalizedToken in packagingOrPresentation ->
                FoodDomainTokenClass.PACKAGING_OR_PRESENTATION

            normalizedToken in otherFoodDomainTokens ->
                FoodDomainTokenClass.OTHER_FOOD_DOMAIN

            else ->
                FoodDomainTokenClass.UNKNOWN
        }

    private fun validateVocabularyDisjointness() {
        val vocabularies =
            listOf(
                FoodDomainTokenClass.STOPWORD to stopwords,
                FoodDomainTokenClass.ANIMAL_SPECIES to animalSpecies,
                FoodDomainTokenClass.ANIMAL_PRODUCT_OR_CUT to animalProductsOrCuts,
                FoodDomainTokenClass.PROCESSED_ANIMAL_PRODUCT to processedAnimalProducts,
                FoodDomainTokenClass.PLANT_INGREDIENT to plantIngredients,
                FoodDomainTokenClass.GRAIN_OR_LEGUME to grainsOrLegumes,
                FoodDomainTokenClass.NUT_SEED_OR_OIL_SOURCE to nutsSeedsOrOilSources,
                FoodDomainTokenClass.HERB_OR_SPICE to herbsOrSpices,
                FoodDomainTokenClass.DAIRY_PRODUCT to dairyProducts,
                FoodDomainTokenClass.DISH_OR_MEAL to dishesOrMeals,
                FoodDomainTokenClass.BAKERY_OR_STARCH_PRODUCT to bakeryOrStarchProducts,
                FoodDomainTokenClass.BEVERAGE to beverages,
                FoodDomainTokenClass.SWEET_PRODUCT to sweetProducts,
                FoodDomainTokenClass.PRODUCT_FORM to productForms,
                FoodDomainTokenClass.PREPARATION_OR_PROCESSING to preparationOrProcessing,
                FoodDomainTokenClass.COLOR_OR_APPEARANCE to colorsOrAppearance,
                FoodDomainTokenClass.REGION_OR_CUISINE to regionsOrCuisines,
                FoodDomainTokenClass.STYLE_OR_QUALITY_MODIFIER to styleOrQualityModifiers,
                FoodDomainTokenClass.QUANTITY_OR_SIZE_MODIFIER to quantityOrSizeModifiers,
                FoodDomainTokenClass.DIET_OR_SUBSTITUTE to dietOrSubstitutes,
                FoodDomainTokenClass.PACKAGING_OR_PRESENTATION to packagingOrPresentation,
                FoodDomainTokenClass.OTHER_FOOD_DOMAIN to otherFoodDomainTokens,
            )

        val classesByToken =
            buildMap<String, MutableList<FoodDomainTokenClass>> {
                vocabularies.forEach { (tokenClass, tokens) ->
                    tokens.forEach { token ->
                        getOrPut(token) {
                            mutableListOf()
                        }.add(tokenClass)
                    }
                }
            }

        val duplicateAssignments =
            classesByToken
                .filterValues { tokenClasses ->
                    tokenClasses.size > 1
                }
                .toSortedMap()

        require(duplicateAssignments.isEmpty()) {
            buildString {
                append(
                    "Food-Domain vocabulary contains tokens assigned to " +
                            "multiple classes:",
                )

                duplicateAssignments.forEach { (token, tokenClasses) ->
                    append("\n")
                    append(token)
                    append(" -> ")
                    append(
                        tokenClasses
                            .map { tokenClass ->
                                tokenClass.name
                            }
                            .sorted(),
                    )
                }
            }
        }
    }

    private companion object {

        val stopwords =
            setOf(
                "a",
                "an",
                "and",
                "as",
                "at",
                "by",
                "for",
                "from",
                "in",
                "of",
                "on",
                "or",
                "the",
                "to",
                "with",
            )

        val animalSpecies =
            setOf(
                "abalone",
                "anchovy",
                "beef",
                "bison",
                "boar",
                "calf",
                "carp",
                "chicken",
                "cod",
                "crab",
                "duck",
                "eel",
                "fish",
                "goat",
                "goose",
                "herring",
                "lamb",
                "lobster",
                "mackerel",
                "mussel",
                "mutton",
                "octopus",
                "pangasiu",
                "pig",
                "plaice",
                "pork",
                "prawn",
                "rabbit",
                "redfish",
                "salmon",
                "sardine",
                "scallop",
                "shrimp",
                "squid",
                "tilapia",
                "trout",
                "tuna",
                "turkey",
                "veal",
                "venison",
                "whitefish",
            )

        val animalProductsOrCuts =
            setOf(
                "bacon",
                "belly",
                "boneless",
                "breast",
                "chop",
                "collagen",
                "drumstick",
                "fillet",
                "ham",
                "jerky",
                "leg",
                "liver",
                "loin",
                "meat",
                "neck",
                "patty",
                "prosciutto",
                "rib",
                "steak",
                "tongue",
            )

        val processedAnimalProducts =
            setOf(
                "andouille",
                "bockwurst",
                "bratwurst",
                "braunschweiger",
                "chorizo",
                "currywurst",
                "jagdwurst",
                "knackwurst",
                "krakauer",
                "lyoner",
                "mettenden",
                "mettwurst",
                "pinkel",
                "salami",
                "sausage",
                "wurst",
            )

        val plantIngredients =
            setOf(
                "acai",
                "apple",
                "apricot",
                "artichoke",
                "asparagus",
                "aubergine",
                "avocado",
                "banana",
                "beet",
                "beetroot",
                "berry",
                "blackberry",
                "blackbean",
                "blueberry",
                "cabbage",
                "carrot",
                "chanterelle",
                "cherry",
                "chili",
                "chilli",
                "coconut",
                "corn",
                "cranberry",
                "cress",
                "cucumber",
                "damson",
                "edamame",
                "eggplant",
                "fennel",
                "fruit",
                "ginger",
                "grape",
                "hokkaido",
                "jackfruit",
                "kohlrabi",
                "leek",
                "mango",
                "mushroom",
                "onion",
                "orange",
                "paprika",
                "pea",
                "pepperoni",
                "plum",
                "potato",
                "pumpkin",
                "rhubarb",
                "spinach",
                "tomato",
                "vegetable",
                "zucchini",
            )

        val grainsOrLegumes =
            setOf(
                "adlay",
                "amaranth",
                "barley",
                "bean",
                "buckwheat",
                "chickpea",
                "durum",
                "garbanzo",
                "grain",
                "kamut",
                "lentil",
                "lupin",
                "maize",
                "millet",
                "oat",
                "quinoa",
                "rice",
                "rye",
                "semolina",
                "soy",
                "spelt",
                "wheat",
            )

        val nutsSeedsOrOilSources =
            setOf(
                "agave",
                "almond",
                "camelina",
                "canola",
                "cashew",
                "hazelnut",
                "hemp",
                "linseed",
                "peanut",
                "pistachio",
                "seed",
                "sunflower",
                "walnut",
            )

        val herbsOrSpices =
            setOf(
                "ashwagandha",
                "basil",
                "herb",
                "lovage",
                "marjoram",
                "parsley",
                "sage",
            )

        val dairyProducts =
            setOf(
                "butter",
                "cheddar",
                "cheese",
                "cream",
                "creamed",
                "creamy",
                "milk",
                "quark",
                "yogurt",
            )

        val dishesOrMeals =
            setOf(
                "bolognese",
                "bulgogi",
                "casserole",
                "curry",
                "dumpling",
                "fricassee",
                "goulash",
                "gratin",
                "gyro",
                "korma",
                "lasagna",
                "meal",
                "pelmeni",
                "pizza",
                "risotto",
                "salad",
                "samosa",
                "skillet",
                "soup",
                "stew",
                "taco",
            )

        val bakeryOrStarchProducts =
            setOf(
                "baguette",
                "bread",
                "brioche",
                "cake",
                "cornflake",
                "crescent",
                "crispbread",
                "croissant",
                "dough",
                "muesli",
                "noodle",
                "pasta",
                "pretzel",
                "roll",
                "spaghetti",
                "tagliatelle",
            )

        val beverages =
            setOf(
                "ale",
                "beer",
                "bier",
                "cola",
                "juice",
                "lemonade",
                "spritzer",
                "tea",
                "water",
                "wine",
            )

        val sweetProducts =
            setOf(
                "amaretto",
                "bubblegum",
                "candy",
                "chocolate",
                "dessert",
                "honey",
                "sugar",
                "syrup",
            )

        val productForms =
            setOf(
                "braid",
                "bulb",
                "cube",
                "cut",
                "dice",
                "diced",
                "kernel",
                "leaf",
                "piece",
                "roulade",
                "rouleaux",
                "slice",
                "strip",
                "wedge",
                "whole",
            )

        val preparationOrProcessing =
            setOf(
                "air",
                "battered",
                "boiled",
                "carbonated",
                "caramelized",
                "chlorinated",
                "chopped",
                "cooking",
                "crisp",
                "crispy",
                "crushed",
                "dried",
                "fermented",
                "fresh",
                "freshly",
                "frozen",
                "fry",
                "ground",
                "mash",
                "minced",
                "pickled",
                "precooked",
                "pure",
                "roast",
                "scalded",
                "seasoned",
                "shredded",
                "sliced",
                "soured",
                "whipping",
            )

        val colorsOrAppearance =
            setOf(
                "black",
                "bright",
                "brown",
                "cloudy",
                "dark",
                "green",
                "pink",
                "rainbow",
                "red",
                "white",
            )

        val regionsOrCuisines =
            setOf(
                "aegean",
                "afghan",
                "alaska",
                "alaskan",
                "american",
                "arabic",
                "bavarian",
                "belgian",
                "berlin",
                "california",
                "franconian",
                "greek",
                "himalayan",
                "iberico",
                "italian",
                "japanese",
                "korean",
                "northumberland",
                "norwegian",
                "salzburg",
                "thai",
                "vienna",
                "vietnamese",
            )

        val styleOrQualityModifiers =
            setOf(
                "active",
                "artisan",
                "artisanal",
                "assorted",
                "classic",
                "farmhouse",
                "firm",
                "flavor",
                "fusion",
                "original",
                "premium",
                "recipe",
                "soft",
                "spicy",
                "style",
                "super",
                "wild",
            )

        val quantityOrSizeModifiers =
            setOf(
                "mini",
                "mix",
                "mixed",
                "multi",
                "semi",
                "serving",
                "single",
            )

        val dietOrSubstitutes =
            setOf(
                "alternative",
                "meatless",
                "protein",
                "seitan",
                "substitute",
                "tofu",
                "vegan",
                "vegetarian",
            )

        val packagingOrPresentation =
            setOf(
                "glass",
                "packed",
                "vacuum",
            )

        val otherFoodDomainTokens =
            setOf(
                "germ",
                "lake",
                "north",
                "sea",
                "waxy",
                "yeast",
            )
    }
}