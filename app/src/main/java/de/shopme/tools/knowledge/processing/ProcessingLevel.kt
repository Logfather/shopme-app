package de.shopme.tools.knowledge.processing

enum class ProcessingLevel(

    val displayName: String

) {

    NOVA_1(

        "Unverarbeitet"

    ),

    NOVA_2(

        "Verarbeitete Zutat"

    ),

    NOVA_3(

        "Verarbeitet"

    ),

    NOVA_4(

        "Hoch verarbeitet"

    )

}