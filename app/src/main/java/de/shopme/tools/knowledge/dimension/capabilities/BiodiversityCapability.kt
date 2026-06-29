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

class BiodiversityCapability :

    AbstractKnowledgeDimensionCapability() {

    override val id = KnowledgeDimensionId.BIODIVERSITY


    override val section =
        KnowledgeSection.IMPACT

    override val order =
        40

    override val coverageDimension =
        CoverageDimension.BIODIVERSITY

    override fun info() =
        KnowledgeDimensionInfo(
            title = "Biodiversität",
            description = "Diese Dimension beschreibt, wie stark ein Lebensmittel typischerweise mit Auswirkungen auf Artenvielfalt, Lebensräume und ökologische Vielfalt verbunden ist.",
            storedFacts = listOf("Biodiversitäts-Score"),
            evaluation = "Der Knowledge Compiler übernimmt Biodiversitätswerte und leitet daraus eine verständliche Hivra-Einordnung ab.",
            interpretations = listOf(
                KnowledgeDimensionInterpretation("🟢", "Sehr günstig", "Das Lebensmittel ist mit vergleichsweise günstigen Auswirkungen auf Biodiversität verbunden."),
                KnowledgeDimensionInterpretation("🟡", "Mittel", "Das Lebensmittel liegt im mittleren Bereich."),
                KnowledgeDimensionInterpretation("🟧", "Eher ungünstig", "Das Lebensmittel ist mit erhöhter Belastung für Biodiversität verbunden."),
                KnowledgeDimensionInterpretation("🔴", "Ungünstig", "Das Lebensmittel ist mit hoher Belastung für Biodiversität verbunden.")
            )
        )

    override fun result(
        knowledge: FoodKnowledgeEntry
    ): KnowledgeDimensionResult {

        val score =
            knowledge.biodiversity?.score
                ?: return unknownResult()

        val indicator =
            when {
                score >= 80.0 -> KnowledgeIndicator.GREEN
                score >= 60.0 -> KnowledgeIndicator.LIGHTGREEN
                score >= 40.0 -> KnowledgeIndicator.YELLOW
                score >= 20.0 -> KnowledgeIndicator.ORANGE
                else -> KnowledgeIndicator.RED
            }

        return KnowledgeDimensionResult(
            indicator = indicator,
            summary = "Score ${score.toInt()} / 100",
            recommendation = "Die Hivra-Einordnung zeigt, wie günstig oder belastend das Lebensmittel für Biodiversität eingeschätzt wird."
        )
    }
}