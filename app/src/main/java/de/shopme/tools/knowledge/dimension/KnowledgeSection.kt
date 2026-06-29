package de.shopme.tools.knowledge.dimension

enum class KnowledgeSection(

    val title: String,

    val order: Int

) {

    CONTENT(

        title = "WAS STECKT DRIN?",

        order = 10

    ),

    PRODUCTION(

        title = "WIE WIRD ES HERGESTELLT?",

        order = 20

    ),

    IMPACT(

        title = "WELCHE AUSWIRKUNGEN HAT ES?",

        order = 30

    ),

    ORIGIN(

        title = "WOHER KOMMT ES?",

        order = 40

    ),

    RELATIONSHIP(

        title = "WELCHE BEZIEHUNGEN GIBT ES?",

        order = 50

    ),

    INTERPRETATION(

        title =

            "WELCHE BEWERTUNGEN KÖNNEN DARAUS ABGELEITET WERDEN?",

        order = 60

    )

}