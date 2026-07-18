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
import de.shopme.tools.knowledge.ki_candidates.KnowledgeCandidateMerger
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AgribalyseCandidateMergerIntegrationTest {

    @Test
    fun mergeAgribalyseCanonicalCandidatesWithoutDroppingDimensions() {
        val reader = AgribalyseExcelReader()
        val rawProductMapper = AgribalyseRawProductMapper()
        val adapter = AgribalyseAIImportAdapter()
        val builder = AgribalyseCanonicalCandidateBuilder()
        val merger = KnowledgeCandidateMerger()

        val records = reader.readRecords(
            sheetType = AgribalyseSheetType.SYNTHESIS,
            layout = AgribalyseSheetLayout.synthesis,
            maxRecords = 5
        )

        val inputs = records
            .map(rawProductMapper::map)
            .map(adapter::adapt)

        val buildResult = builder.build(
            AIKnowledgeBuildRequest(
                source = AIKnowledgeSourceInfo(
                    type = AIKnowledgeSourceType.AGRIBALYSE,
                    name = "agribalyse",
                    version = "3.2"
                ),
                inputs = inputs
            )
        )

        val mergeResult = merger.merge(buildResult.candidates)

        assertTrue(mergeResult.conflicts.isEmpty())
        assertEquals(5, mergeResult.candidates.size)

        val first = mergeResult.candidates.first {
            it.canonicalId == "11172"
        }

        assertEquals(
            setOf("Court-bouillon pour poissons, déshydraté"),
            first.aliases
        )

        val dimensions = first.dimensions.map { it.dimension }.toSet()

        assertTrue(KnowledgeDimensionCandidateType.CARBON in dimensions)
        assertTrue(KnowledgeDimensionCandidateType.WATER in dimensions)
        assertTrue(KnowledgeDimensionCandidateType.PRODUCTION in dimensions)
        assertTrue(KnowledgeDimensionCandidateType.TAXONOMY in dimensions)
    }
}