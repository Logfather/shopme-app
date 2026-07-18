package de.shopme.testing.system.tools.knowledge.agribalyse

import de.shopme.tools.knowledge.agribalyse.adapter.AgribalyseAIImportAdapter
import de.shopme.tools.knowledge.agribalyse.loader.AgribalyseExcelReader
import de.shopme.tools.knowledge.agribalyse.mapper.AgribalyseCanonicalCandidateBuilder
import de.shopme.tools.knowledge.agribalyse.mapper.AgribalyseRawProductMapper
import de.shopme.tools.knowledge.agribalyse.model.AgribalyseSheetLayout
import de.shopme.tools.knowledge.agribalyse.model.AgribalyseSheetType
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildRequest
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildResult
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceInfo
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceType
import de.shopme.tools.knowledge.ai.builder.DefaultAIKnowledgeBuilderPipeline
import de.shopme.tools.knowledge.ai.builder.DefaultAIKnowledgeBuilderResolver
import de.shopme.tools.knowledge.compiler.catalog.AIKnowledgeCatalogResultImporter
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AgribalyseCatalogImportIntegrationTest {

    @Test
    fun run_importsAgribalyseKnowledgeIntoCatalogImportWorkflow() {
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

        val capturedResults = mutableListOf<AIKnowledgeBuildResult>()

        val pipeline = DefaultAIKnowledgeBuilderPipeline(
            builderResolver = DefaultAIKnowledgeBuilderResolver(
                buildersBySourceType = mapOf(
                    AIKnowledgeSourceType.AGRIBALYSE to builder
                )
            ),
            catalogUpdateWorkflow = object : AIKnowledgeCatalogResultImporter {
                override fun importAIKnowledge(
                    result: AIKnowledgeBuildResult
                ) {
                    capturedResults += result
                }
            }
        )

        val result = pipeline.run(
            request = AIKnowledgeBuildRequest(
                source = AIKnowledgeSourceInfo(
                    type = AIKnowledgeSourceType.AGRIBALYSE,
                    name = "agribalyse",
                    version = "3.2"
                ),
                inputs = inputs
            )
        )

        assertEquals(5, result.candidates.size)
        assertEquals(1, capturedResults.size)
        assertEquals(result, capturedResults.single())

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