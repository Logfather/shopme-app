package de.shopme.testing.system.tools.knowledge.carbon

import de.shopme.tools.knowledge.carbon.builder.CarbonKnowledgeBuilder
import de.shopme.tools.knowledge.carbon.importer.AgribalyseCarbonImporter
import de.shopme.tools.knowledge.carbon.importer.OFFCarbonImporter
import de.shopme.tools.knowledge.carbon.mapper.CarbonCandidateMapper
import de.shopme.tools.knowledge.carbon.merge.CarbonCandidateMerger
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class MultiSourceCarbonKnowledgeBuilderTest {

    @Test
    fun buildsCarbonKnowledgeFromMultipleSources() {

        val agribalyseFile =
            File.createTempFile(
                "agribalyse-synthese",
                ".csv"
            )

        agribalyseFile.writeText(
            """
            product;carbon
            banana;0.91
            """.trimIndent()
        )

        val builder =
            CarbonKnowledgeBuilder(

                importers =
                    listOf(

                        OFFCarbonImporter(
                            entries =
                                mapOf(
                                    "apple" to 0.43
                                )
                        ),

                        AgribalyseCarbonImporter(
                            file = agribalyseFile,
                            productColumn = "product",
                            carbonColumn = "carbon"
                        )
                    ),

                merger =
                    CarbonCandidateMerger(),

                mapper =
                    CarbonCandidateMapper()
            )

        val result =
            builder.build()

        assertEquals(
            2,
            result.size
        )

        assertEquals(
            0.43,
            requireNotNull(result["apple"])
                .kilogramsPerKilogram,
            0.0001
        )

        assertEquals(
            0.91,
            requireNotNull(result["banana"])
                .kilogramsPerKilogram,
            0.0001
        )

        agribalyseFile.delete()
    }
}