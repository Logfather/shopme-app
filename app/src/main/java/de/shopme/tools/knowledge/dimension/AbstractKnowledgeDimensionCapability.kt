package de.shopme.tools.knowledge.dimension

abstract class AbstractKnowledgeDimensionCapability :

    KnowledgeDimensionCapability {

    protected fun unknownResult() =

        KnowledgeDimensionResult(

            indicator =

                KnowledgeIndicator.UNKNOWN,

            summary =

                "Keine Daten verfügbar",

            recommendation =

                "Für diese Wissensdimension liegen derzeit keine Informationen vor."

        )

    override val order: Int =
        1000

}