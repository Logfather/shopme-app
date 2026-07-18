package de.shopme.tools.knowledge.agribalyse.mapper

import de.shopme.tools.knowledge.agribalyse.model.AgribalyseCandidateData
import de.shopme.tools.knowledge.ai.builder.RawKnowledgeInput
import de.shopme.tools.knowledge.ai.builder.double
import de.shopme.tools.knowledge.ai.builder.string
import de.shopme.tools.knowledge.ai.builder.stringList

class AgribalyseCandidateExtractor {

    fun extract(
        input: RawKnowledgeInput
    ): AgribalyseCandidateData =
        AgribalyseCandidateData(
            sourceId = input.sourceId,
            name = input.string("name"),

            taxonomy = input.stringList("taxonomy"),
            production = input.stringList("production"),

            carbon = input.double("carbon"),
            water = input.double("water"),

            dataQualityScore = input.double("dataQualityScore"),
            singleScoreMptPerKg = input.double("singleScoreMptPerKg"),
            landUsePtPerKg = input.double("landUsePtPerKg"),
            energyMjPerKg = input.double("energyMjPerKg"),

            biogenicCarbonKgCo2EqPerKg = input.double("biogenicCarbonKgCo2EqPerKg"),
            fossilCarbonKgCo2EqPerKg = input.double("fossilCarbonKgCo2EqPerKg"),
            landUseChangeCarbonKgCo2EqPerKg = input.double("landUseChangeCarbonKgCo2EqPerKg")
        )
}