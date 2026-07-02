package de.shopme.testing.system.tools.knowledge.compiler.catalog

import com.google.gson.GsonBuilder
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildResult
import de.shopme.tools.knowledge.compiler.catalog.DefaultFileCatalogUpdateWorkflowFactory
import de.shopme.tools.knowledge.compiler.catalog.FileCatalogReader
import de.shopme.tools.knowledge.compiler.catalog.GsonCatalogJsonDeserializer
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FileCatalogAIImportIntegrationTest {

    @Test
    fun importAIKnowledge_addsCandidateToFoodsJson() {

        val file =
            File.createTempFile(
                "foods",
                ".json"
            )

        file.writeText(
            """
            [
              {
                "itemname": "Apple",
                "category": "fruit",
                "production": "fresh",
                "normalized": "apple",
                "plural": "apples",
                "colloquial": [],
                "phonetic_tokens": [],
                "autocomplete_tokens": []
              }
            ]
            """.trimIndent()
        )

        val workflow =
            DefaultFileCatalogUpdateWorkflowFactory.create(file)

        val result =
            AIKnowledgeBuildResult(
                candidates = listOf(
                    CanonicalKnowledgeCandidate(
                        canonicalId = "banana",
                        aliases = setOf(
                            "banana"
                        ),
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

        workflow.importAIKnowledge(result)

        val gson =
            GsonBuilder()
                .setPrettyPrinting()
                .create()

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
            updatedCatalog.map { it.normalized }
        )

        assertTrue(
            updatedCatalog.any {
                it.normalized == "banana"
            }
        )
    }
}