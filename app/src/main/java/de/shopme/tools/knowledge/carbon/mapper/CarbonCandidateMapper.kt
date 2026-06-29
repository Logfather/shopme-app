package de.shopme.tools.knowledge.carbon.mapper

import de.shopme.tools.knowledge.carbon.CarbonFootprint
import de.shopme.tools.knowledge.carbon.model.CarbonKnowledgeCandidate
import de.shopme.tools.knowledge.source.KnowledgeCandidateMapper

class CarbonCandidateMapper :

    KnowledgeCandidateMapper<
            CarbonKnowledgeCandidate,
            CarbonFootprint
            >  {

    override fun map(
        candidate: CarbonKnowledgeCandidate
    ): CarbonFootprint {

        return CarbonFootprint(

            kilogramsPerKilogram =
                candidate.kgCo2ePerKg

        )
    }
}