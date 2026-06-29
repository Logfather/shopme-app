package de.shopme.testing.system.tools.knowledge.carbon

import de.shopme.tools.knowledge.carbon.builder.CarbonKnowledgeBuilder
import de.shopme.tools.knowledge.carbon.importer.OFFCarbonImporter
import de.shopme.tools.knowledge.carbon.mapper.CarbonCandidateMapper
import de.shopme.tools.knowledge.carbon.merge.CarbonCandidateMerger
import de.shopme.tools.knowledge.carbon.merge.CarbonMergeReportPrinter
import de.shopme.tools.knowledge.compiler.writer.CarbonKnowledgeWriter
import org.junit.Assert.assertEquals
import org.junit.Test

class CarbonKnowledgeBuilderIntegrationTest {

    @Test
    fun writesBuiltCarbonKnowledgeIntoWriter() {

        val builder =
            CarbonKnowledgeBuilder(

                importers =
                    listOf(

                        OFFCarbonImporter(

                            entries =
                                mapOf(

                                    "apple" to 0.43

                                )
                        )
                    ),

                merger =
                    CarbonCandidateMerger(),

                mapper =
                    CarbonCandidateMapper()
            )

        val writer =
            CarbonKnowledgeWriter(

                builder = builder

            )

        writer.finish()

        val knowledge =
            writer.knowledge()

        val apple =
            requireNotNull(

                knowledge.entries["apple"]

            )

        assertEquals(

            0.43,

            apple.kilogramsPerKilogram,

            0.0001
        )
    }
}