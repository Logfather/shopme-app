package de.shopme.testing.system.tools.knowledge.ai.builder

import com.google.gson.GsonBuilder
import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildRequest
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildResult
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuilder
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceInfo
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceType
import de.shopme.tools.knowledge.ai.builder.DefaultAIKnowledgeBuilderPipelineFactory
import de.shopme.tools.knowledge.compiler.catalog.FileCatalogReader
import de.shopme.tools.knowledge.compiler.catalog.GsonCatalogJsonDeserializer
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class DefaultAIKnowledgeBuilderPipelineFactoryTest {

    @Test
    fun createReturnsPipelineThatUpdatesCatalogFile() {

        val file =
            File.createTempFile(
                "foods",
                ".json"
            )

        try {
            val gson =
                GsonBuilder()
                    .setPrettyPrinting()
                    .create()

            file.writeText(
                gson.toJson(
                    listOf(
                        catalogItem("apple")
                    )
                )
            )

            val pipeline =
                DefaultAIKnowledgeBuilderPipelineFactory.create(
                    catalogFile = file,
                    buildersBySourceType = mapOf(
                        AIKnowledgeSourceType.CUSTOM to FakeAIKnowledgeBuilder()
                    )
                )

            pipeline.run(
                AIKnowledgeBuildRequest(
                    source = AIKnowledgeSourceInfo(
                        type = AIKnowledgeSourceType.CUSTOM,
                        name = "Test",
                        version = "1.0"
                    ),
                    inputs = emptyList()
                )
            )

            val updatedCatalog =
                FileCatalogReader(
                    inputFile = file,
                    deserializer = GsonCatalogJsonDeserializer(gson)
                ).read()

            assertEquals(
                listOf(
                    "apple",
                    "banana"
                ),
                updatedCatalog.map {
                    it.normalized
                }
            )

        } finally {
            file.delete()
        }
    }

    private class FakeAIKnowledgeBuilder : AIKnowledgeBuilder {

        override fun build(
            request: AIKnowledgeBuildRequest
        ): AIKnowledgeBuildResult {

            return AIKnowledgeBuildResult(
                candidates = listOf(
                    CanonicalKnowledgeCandidate(
                        canonicalId = "banana",
                        aliases = setOf("banana"),
                        dimensions = emptyList(),
                        metadata = CandidateMetadata(
                            source = "test",
                            sourceId = "test-banana",
                            confidence = 1.0,
                            version = "test"
                        )
                    )
                )
            )
        }
    }

    companion object {

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