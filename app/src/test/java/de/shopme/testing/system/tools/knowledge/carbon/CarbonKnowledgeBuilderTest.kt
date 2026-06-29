package de.shopme.testing.system.tools.knowledge.carbon

import de.shopme.tools.knowledge.carbon.builder.CarbonKnowledgeBuilder
import de.shopme.tools.knowledge.carbon.importer.CarbonSourceImporter
import de.shopme.tools.knowledge.carbon.mapper.CarbonCandidateMapper
import de.shopme.tools.knowledge.carbon.merge.CarbonCandidateMerger
import de.shopme.tools.knowledge.carbon.model.CarbonKnowledgeCandidate
import de.shopme.tools.knowledge.source.KnowledgeSourceId
import org.junit.Assert.assertEquals
import org.junit.Test

class CarbonKnowledgeBuilderTest {

    @Test
    fun buildsCarbonKnowledgeFromCandidates() {

        val importer =
            object : CarbonSourceImporter {

                override fun load():
                        List<CarbonKnowledgeCandidate> {

                    return listOf(

                        CarbonKnowledgeCandidate(

                            reference = "apple",

                            kgCo2ePerKg = 0.43,

                            source =
                                KnowledgeSourceId.OPEN_FOOD_FACTS

                        )

                    )
                }
            }

        val builder =
            CarbonKnowledgeBuilder(

                importers =
                    listOf(importer),

                merger =
                    CarbonCandidateMerger(),

                mapper =
                    CarbonCandidateMapper()

            )

        val result =
            builder.build()

        assertEquals(

            1,

            result.size

        )

        val apple =
            requireNotNull(
                result["apple"]
            )

        assertEquals(
            0.43,
            apple.kilogramsPerKilogram,
            0.0001
        )
    }
}