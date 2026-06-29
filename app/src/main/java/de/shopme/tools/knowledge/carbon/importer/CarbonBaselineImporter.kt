package de.shopme.tools.knowledge.carbon.importer

import de.shopme.tools.knowledge.carbon.StringCarbonFootprintLoader
import de.shopme.tools.knowledge.carbon.model.CarbonKnowledgeCandidate
import de.shopme.tools.knowledge.source.KnowledgeSourceId
import java.io.File

class CarbonBaselineImporter(

    private val file: File

) : CarbonSourceImporter {

    override fun load(): List<CarbonKnowledgeCandidate> {

        if (!file.exists()) {
            return emptyList()
        }

        val knowledge =
            StringCarbonFootprintLoader(
                file.readText()
            ).load()

        return knowledge.entries.map { entry ->

            CarbonKnowledgeCandidate(

                reference =
                    entry.key,

                kgCo2ePerKg =
                    entry.value.kilogramsPerKilogram,

                source =
                    KnowledgeSourceId.OPEN_FOOD_FACTS
            )
        }
    }
}