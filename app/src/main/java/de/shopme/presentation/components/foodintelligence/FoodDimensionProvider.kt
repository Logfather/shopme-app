package de.shopme.presentation.components.foodintelligence

object FoodDimensionProvider {

    private val dimensions = listOf(

        FoodDimension(

            section = FoodIntelligenceSection.NUTRITION,

            displayOrder = 10,

            category = FoodDimensionCategory.HEALTH,

            title = "Ernährung",

            shortTitle = "Ernährung",

            description =
                "Der Nutri-Score fasst verschiedene Nährwerte zu einer leicht verständlichen Gesamtbewertung zusammen."

        ),

        FoodDimension(

            section = FoodIntelligenceSection.CARBON,

            displayOrder = 20,

            category = FoodDimensionCategory.SUSTAINABILITY,

            title = "CO₂-Fußabdruck",

            shortTitle = "CO₂",

            description =
                "Der CO₂-Fußabdruck beschreibt die Menge an Treibhausgasen, die bei Produktion und Transport entstehen."
        ),

        FoodDimension(

            section = FoodIntelligenceSection.WATER,

            displayOrder = 30,

            category = FoodDimensionCategory.SUSTAINABILITY,

            title = "Wasserverbrauch",

            shortTitle = "Wasser",

            description =
                "Der Wasserverbrauch beschreibt den gesamten Wasserbedarf eines Lebensmittels."
        ),

        FoodDimension(

            section = FoodIntelligenceSection.PROCESSING,

            title = "Verarbeitungsgrad",

            displayOrder = 40,

            category = FoodDimensionCategory.HEALTH,

            shortTitle = "Verarbeitung",

            description =
                "Der Verarbeitungsgrad bewertet die Intensität der industriellen Verarbeitung eines Lebensmittels.",
            ),

        FoodDimension(

            section = FoodIntelligenceSection.PACKAGING,

            title = "Verpackung",

            displayOrder = 40,

            category = FoodDimensionCategory.HEALTH,

            shortTitle = "Verpackung",

            description =
                "Die Verpackungsbewertung berücksichtigt Materialeinsatz und Recyclingfähigkeit.",
            ),

        FoodDimension(

            section = FoodIntelligenceSection.BIODIVERSITY,

            title = "Biodiversität",

            displayOrder = 60,

            category = FoodDimensionCategory.SUSTAINABILITY,

            shortTitle = "Biodiversität",

            description =
                "Die Biodiversität beschreibt den Einfluss eines Lebensmittels auf die biologische Vielfalt.",
            ),

        FoodDimension(

            section = FoodIntelligenceSection.POLLINATOR,

            title = "Bestäuber",

            displayOrder = 70,

            category = FoodDimensionCategory.SUSTAINABILITY,

            shortTitle = "Bestäuber",

            description =
                "Der Bestäuberwert beschreibt den Einfluss auf Bestäuber wie Bienen und andere Insekten.",
            ),

        FoodDimension(

            section = FoodIntelligenceSection.LOCALITY,

            title = "Regionalität",

            displayOrder = 80,

            category = FoodDimensionCategory.REGIONALITY,

            shortTitle = "Regionalität",

            description =
                "Die Regionalität beschreibt Herkunft und Transportweg eines Lebensmittels.",
            ),

        FoodDimension(

            section = FoodIntelligenceSection.FAIR_TRADE,

            displayOrder = 110,

            category = FoodDimensionCategory.SUSTAINABILITY,

            title = "Fair Trade",

            shortTitle = "Fair Trade",

            description =
                "Bewertet, ob ein Lebensmittel unter anerkannten Fair-Trade-Standards produziert oder gehandelt wird und soziale sowie wirtschaftliche Verantwortung entlang der Lieferkette unterstützt."

        ),

        FoodDimension(

            section = FoodIntelligenceSection.ANIMAL_WELFARE,

            displayOrder = 120,

            category = FoodDimensionCategory.SUSTAINABILITY,

            title = "Tierwohl",

            shortTitle = "Tierwohl",

            description =
                "Bewertet die Haltungsbedingungen und den Umgang mit Nutztieren. Höhere Bewertungen stehen für bessere Standards bei Haltung, Pflege und Lebensbedingungen."

        ),

        FoodDimension(

            section = FoodIntelligenceSection.PESTICIDE,

            displayOrder = 130,

            category = FoodDimensionCategory.SUSTAINABILITY,

            title = "Pestizide",

            shortTitle = "Pestizide",

            description =
                "Bewertet das erwartete Risiko einer Belastung durch Pflanzenschutzmittel. Geringere Belastungen führen zu einer besseren Bewertung."

        )

    )

    fun provide(): List<FoodDimension> {

        return dimensions

            .sortedBy {

                it.displayOrder

            }

    }

    fun find(

        section: FoodIntelligenceSection

    ): FoodDimension {

        return dimensions.first {

            it.section == section

        }

    }

}