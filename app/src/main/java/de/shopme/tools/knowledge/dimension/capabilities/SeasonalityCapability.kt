package de.shopme.tools.knowledge.dimension.capabilities

import de.shopme.domain.food.FoodKnowledgeEntry
import de.shopme.tools.knowledge.dimension.KnowledgeSection
import de.shopme.tools.knowledge.dimension.AbstractKnowledgeDimensionCapability
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionInfo
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionInterpretation
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionResult
import de.shopme.tools.knowledge.dimension.KnowledgeIndicator
import de.shopme.tools.report.CoverageDimension

class SeasonalityCapability : AbstractKnowledgeDimensionCapability() {

    override val id = KnowledgeDimensionId.SEASONALITY

    override val section =
        KnowledgeSection.ORIGIN

    override val order =
        30

    override val coverageDimension =
        CoverageDimension.SEASONALITY

    override fun info() =
        KnowledgeDimensionInfo(
            title = "Saisonalität",
            description = "Diese Dimension beschreibt, in welchen Monaten ein Lebensmittel typischerweise saisonal verfügbar ist.",
            storedFacts = listOf("Saisonmonate"),
            evaluation = "Der Knowledge Compiler übernimmt Saisonmonate und leitet daraus eine Hivra-Einordnung ab.",
            interpretations = listOf(
                KnowledgeDimensionInterpretation("🟢", "Sehr breit saisonal", "Das Lebensmittel ist in vielen Monaten saisonal verfügbar."),
                KnowledgeDimensionInterpretation("🟩", "Breit saisonal", "Das Lebensmittel ist in mehreren Monaten saisonal verfügbar."),
                KnowledgeDimensionInterpretation("🟡", "Begrenzt saisonal", "Das Lebensmittel hat eine klar begrenzte Saison."),
                KnowledgeDimensionInterpretation("🟧", "Kurz saisonal", "Das Lebensmittel ist nur kurz saisonal verfügbar."),
                KnowledgeDimensionInterpretation("🔴", "Keine Saisondaten", "Für dieses Lebensmittel liegen keine Saisondaten vor.")
            )
        )

    override fun result(knowledge: FoodKnowledgeEntry): KnowledgeDimensionResult {
        val months = knowledge.seasonality
        if (months.isEmpty()) return unknownResult()

        val indicator =
            when {
                months.size >= 9 -> KnowledgeIndicator.GREEN
                months.size >= 6 -> KnowledgeIndicator.LIGHTGREEN
                months.size >= 3 -> KnowledgeIndicator.YELLOW
                months.size >= 1 -> KnowledgeIndicator.ORANGE
                else -> KnowledgeIndicator.RED
            }

        return KnowledgeDimensionResult(
            indicator = indicator,
            summary = "Verfügbar: ${formatMonths(months)}",
            recommendation = "Die Hivra-Einordnung zeigt, wie breit die saisonale Verfügbarkeit typischerweise ist."
        )
    }

    private fun formatMonths(months: Collection<Int>): String =
        months
            .sorted()
            .joinToString(", ") {
                monthName(it)
            }

    private fun monthName(month: Int): String =
        when (month) {
            1 -> "Jan"
            2 -> "Feb"
            3 -> "Mär"
            4 -> "Apr"
            5 -> "Mai"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Aug"
            9 -> "Sep"
            10 -> "Okt"
            11 -> "Nov"
            12 -> "Dez"
            else -> "?"
        }
}