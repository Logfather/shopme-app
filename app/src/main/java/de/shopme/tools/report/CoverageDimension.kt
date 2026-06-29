package de.shopme.tools.report

import de.shopme.tools.knowledge.dimension.KnowledgeSection

enum class CoverageDimension(

    val displayName: String,

    val section: KnowledgeSection,

    val order: Int

) {

    // WAS STECKT DRIN?

    NUTRITION(

        "Ernährung",

        KnowledgeSection.CONTENT,

        10

    ),

    ALLERGEN(

        "Allergene",

        KnowledgeSection.CONTENT,

        20

    ),

    GLYCEMIC(

        "Glykämischer Index",

        KnowledgeSection.CONTENT,

        30

    ),

    DIET(

        "Ernährungsformen",

        KnowledgeSection.CONTENT,

        40

    ),

    TAXONOMY(

        "Lebensmittelklassifikation",

        KnowledgeSection.CONTENT,

        50

    ),

    // WIE WIRD ES HERGESTELLT?

    PRODUCTION(

        "Herstellung",

        KnowledgeSection.PRODUCTION,

        100

    ),

    PROCESSING(

        "Verarbeitung",

        KnowledgeSection.PRODUCTION,

        110

    ),

    PACKAGING(

        "Verpackung",

        KnowledgeSection.PRODUCTION,

        120

    ),

    // WELCHE AUSWIRKUNGEN HAT ES?

    CARBON(

        "CO₂-Fußabdruck",

        KnowledgeSection.IMPACT,

        200

    ),

    CARBONIMPACT(

        "Klimaauswirkung",

        KnowledgeSection.IMPACT,

        210

    ),

    WATER(

        "Wasserverbrauch",

        KnowledgeSection.IMPACT,

        220

    ),

    WATERSTRESS(

        "Wasservorräte",

        KnowledgeSection.IMPACT,

        230

    ),

    BIODIVERSITY(

        "Biodiversität",

        KnowledgeSection.IMPACT,

        240

    ),

    POLLINATOR(

        "Bestäuber",

        KnowledgeSection.IMPACT,

        250

    ),

    PESTICIDE(

        "Pestizide",

        KnowledgeSection.IMPACT,

        260

    ),

    ANIMALWELFARE(

        "Tierwohl",

        KnowledgeSection.IMPACT,

        270

    ),

    // WOHER KOMMT ES?

    LOCALITY(

        "Regionalität",

        KnowledgeSection.ORIGIN,

        300

    ),

    FOODMILES(

        "Transportwege",

        KnowledgeSection.ORIGIN,

        310

    ),

    FAIRTRADE(

        "Fair Trade",

        KnowledgeSection.ORIGIN,

        320

    ),

    SEASONALITY(

        "Saisonalität",

        KnowledgeSection.ORIGIN,

        330

    ),

    // WELCHE BEZIEHUNGEN GIBT ES?

    INGREDIENT(

        "Zutatenliste",

        KnowledgeSection.RELATIONSHIP,

        400

    ),

    RECIPE(

        "Rezeptliste",

        KnowledgeSection.RELATIONSHIP,

        410

    ),

    // WELCHE BEWERTUNGEN KÖNNEN DARAUS ABGELEITET WERDEN?

    NUTRISCORE(

        "Nutri Score",

        KnowledgeSection.INTERPRETATION,

        500

    )

}