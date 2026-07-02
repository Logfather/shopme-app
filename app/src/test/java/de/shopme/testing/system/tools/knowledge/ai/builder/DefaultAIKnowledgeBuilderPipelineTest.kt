package de.shopme.testing.system.tools.knowledge.ai.builder

import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildRequest
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildResult
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuilder
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuilderResolver
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceInfo
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceType
import de.shopme.tools.knowledge.ai.builder.DefaultAIKnowledgeBuilderPipeline
import de.shopme.tools.knowledge.ai.builder.RawKnowledgeInput
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatch
import de.shopme.tools.knowledge.compiler.catalog.AIKnowledgeCatalogUpdateWorkflow
import de.shopme.tools.knowledge.compiler.catalog.AIKnowledgeCatalogImportWorkflow
import de.shopme.tools.knowledge.compiler.catalog.CatalogJsonDeserializer
import de.shopme.tools.knowledge.compiler.catalog.FileCatalogReader
import de.shopme.tools.knowledge.compiler.catalog.FileCatalogUpdateWorkflow
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class DefaultAIKnowledgeBuilderPipelineTest {

    @Test
    fun runBuildsKnowledgeAndImportsResultIntoCatalogWorkflow() {

        val expectedResult =
            AIKnowledgeBuildResult(
                candidates = emptyList()
            )

        val builder =
            FakeAIKnowledgeBuilder(
                result = expectedResult
            )

        val importWorkflow =
            FakeAIKnowledgeCatalogImportWorkflow()

        val file =
            File.createTempFile(
                "catalog",
                ".json"
            )

        try {
            file.writeText("TEST_JSON")

            val workflow =
                FileCatalogUpdateWorkflow(
                    reader = FileCatalogReader(
                        deserializer = FakeCatalogJsonDeserializer(),
                        inputFile = file
                    ),
                    updateWorkflow = FakeAIKnowledgeCatalogUpdateWorkflow(),
                    importWorkflow = importWorkflow
                )

            val pipeline =
                DefaultAIKnowledgeBuilderPipeline(
                    builderResolver = FakeAIKnowledgeBuilderResolver(
                        builder = builder
                    ),
                    catalogUpdateWorkflow = workflow
                )

            val request =
                testRequest()

            val actualResult =
                pipeline.run(request)

            assertEquals(
                expectedResult,
                actualResult
            )

            assertEquals(
                request,
                builder.receivedRequest
            )

            assertEquals(
                expectedResult,
                importWorkflow.receivedResult
            )

            assertEquals(
                listOf("apple"),
                importWorkflow.receivedCatalog.map {
                    it.normalized
                }
            )

        } finally {
            file.delete()
        }
    }

    private class FakeAIKnowledgeBuilder(
        private val result: AIKnowledgeBuildResult
    ) : AIKnowledgeBuilder {

        lateinit var receivedRequest: AIKnowledgeBuildRequest

        override fun build(
            request: AIKnowledgeBuildRequest
        ): AIKnowledgeBuildResult {

            receivedRequest = request
            return result
        }
    }

    private class FakeAIKnowledgeCatalogImportWorkflow :
        AIKnowledgeCatalogImportWorkflow {

        lateinit var receivedCatalog: List<CatalogItem>
        lateinit var receivedResult: AIKnowledgeBuildResult

        override fun importAIKnowledge(
            catalog: List<CatalogItem>,
            result: AIKnowledgeBuildResult
        ) {

            receivedCatalog = catalog
            receivedResult = result
        }
    }

    private class FakeAIKnowledgeCatalogUpdateWorkflow :
        AIKnowledgeCatalogUpdateWorkflow {

        override fun updateCatalog(
            catalog: List<CatalogItem>,
            patch: FoodsJsonPatch
        ) {
            throw UnsupportedOperationException(
                "Not used in this test."
            )
        }
    }

    private class FakeCatalogJsonDeserializer :
        CatalogJsonDeserializer {

        override fun deserialize(
            json: String
        ): List<CatalogItem> {

            assertEquals(
                "TEST_JSON",
                json
            )

            return listOf(
                catalogItem("apple")
            )
        }
    }

    private class FakeAIKnowledgeBuilderResolver(
        private val builder: AIKnowledgeBuilder
    ) : AIKnowledgeBuilderResolver {

        lateinit var receivedRequest: AIKnowledgeBuildRequest

        override fun resolve(
            request: AIKnowledgeBuildRequest
        ): AIKnowledgeBuilder {

            receivedRequest = request
            return builder
        }
    }

    companion object {

        private fun testRequest(): AIKnowledgeBuildRequest {

            return AIKnowledgeBuildRequest(
                source = AIKnowledgeSourceInfo(
                    type = AIKnowledgeSourceType.OPEN_FOOD_FACTS,
                    name = "Open Food Facts",
                    version = "1.0"
                ),
                inputs = listOf(
                    RawKnowledgeInput(
                        sourceId = "banana",
                        fields = mapOf(
                            "name" to "banana"
                        )
                    )
                )
            )
        }

        private fun catalogItem(
            normalized: String
        ): CatalogItem {

            return CatalogItem(
                itemname = normalized,
                category = "",
                production = "",
                normalized = normalized,
                plural = "",
                colloquial = emptyList(),
                phonetic_tokens = emptyList(),
                autocomplete_tokens = emptyList()
            )
        }
    }
}