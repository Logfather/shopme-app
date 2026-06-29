package de.shopme.tools.knowledge.agribalyse.mapper

import de.shopme.tools.knowledge.agribalyse.model.AgribalyseSyntheseRow
import de.shopme.tools.knowledge.carbon.model.CarbonKnowledgeCandidate
import de.shopme.tools.knowledge.source.KnowledgeSourceId

class AgribalyseCarbonCandidateMapper(

    private val referenceMapper: AgribalyseReferenceMapper =
        AgribalyseReferenceMapper()

) {

    private var lastReferenceMapped: Boolean =
        false

    fun wasLastReferenceMapped(): Boolean =
        lastReferenceMapped

    fun map(
        row: AgribalyseSyntheseRow
    ): CarbonKnowledgeCandidate {

        val mappingResult =
            referenceMapper.map(
                row.productName
            )

        lastReferenceMapped =
            mappingResult.mapped

        return CarbonKnowledgeCandidate(

            reference =
                mappingResult.reference,

            kgCo2ePerKg =
                row.climateChangeKgCo2ePerKg,

            source =
                KnowledgeSourceId.AGRIBALYSE
        )
    }

    fun mappedCount(): Int =
        referenceMapper.mappedCount()
}