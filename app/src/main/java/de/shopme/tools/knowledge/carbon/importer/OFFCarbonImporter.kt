package de.shopme.tools.knowledge.carbon.importer

import de.shopme.tools.knowledge.carbon.model.CarbonKnowledgeCandidate
import de.shopme.tools.knowledge.source.KnowledgeSourceId

class OFFCarbonImporter(

    private val entries: Map<String, Double>

) : CarbonSourceImporter {

    override fun load():
            List<CarbonKnowledgeCandidate> {

        return entries.map { (reference, kgCo2ePerKg) ->

            CarbonKnowledgeCandidate(

                reference = reference,

                kgCo2ePerKg = kgCo2ePerKg,

                source = KnowledgeSourceId.OPEN_FOOD_FACTS

            )
        }
    }
}