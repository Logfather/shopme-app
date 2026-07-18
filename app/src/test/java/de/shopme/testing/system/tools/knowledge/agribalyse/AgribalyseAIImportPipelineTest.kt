package de.shopme.testing.system.tools.knowledge.agribalyse

import de.shopme.tools.knowledge.agribalyse.adapter.AgribalyseAIImportAdapter
import de.shopme.tools.knowledge.agribalyse.loader.AgribalyseExcelReader
import de.shopme.tools.knowledge.agribalyse.mapper.AgribalyseCanonicalCandidateBuilder
import de.shopme.tools.knowledge.agribalyse.mapper.AgribalyseRawProductMapper
import de.shopme.tools.knowledge.agribalyse.model.AgribalyseSheetLayout
import de.shopme.tools.knowledge.agribalyse.model.AgribalyseSheetType
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildRequest
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceInfo
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceType
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AgribalyseAIImportPipelineTest {

    @Test
    fun buildCanonicalCandidatesFromAgribalyseSynthesisRows() {
        val reader = AgribalyseExcelReader()
        val rawProductMapper = AgribalyseRawProductMapper()
        val adapter = AgribalyseAIImportAdapter()
        val builder = AgribalyseCanonicalCandidateBuilder()

        val records = reader.readRecords(
            sheetType = AgribalyseSheetType.SYNTHESIS,
            layout = AgribalyseSheetLayout.synthesis,
            maxRecords = 5
        )

        val inputs = records
            .map(rawProductMapper::map)
            .map(adapter::adapt)

        val result = builder.build(
            AIKnowledgeBuildRequest(
                source = AIKnowledgeSourceInfo(
                    type = AIKnowledgeSourceType.AGRIBALYSE,
                    name = "agribalyse",
                    version = "3.2"
                ),
                inputs = inputs
            )
        )

        assertEquals(5, result.candidates.size)

        val first = result.candidates.first()

        assertEquals("11172", first.canonicalId)
        assertTrue("Court-bouillon pour poissons, déshydraté" in first.aliases)

        assertTrue(
            first.dimensions.any {
                it.dimension == KnowledgeDimensionCandidateType.CARBON &&
                        it.payload == 7.58
            }
        )

        assertTrue(
            first.dimensions.any {
                it.dimension == KnowledgeDimensionCandidateType.WATER &&
                        it.payload == 3.38
            }
        )

        assertTrue(
            first.dimensions.any {
                it.dimension == KnowledgeDimensionCandidateType.PRODUCTION
            }
        )

        assertTrue(
            first.dimensions.any {
                it.dimension == KnowledgeDimensionCandidateType.TAXONOMY
            }
        )
    }
}