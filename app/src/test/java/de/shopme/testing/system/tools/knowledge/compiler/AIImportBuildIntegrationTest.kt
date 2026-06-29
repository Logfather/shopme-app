package de.shopme.testing.system.tools.knowledge.compiler

import de.shopme.tools.knowledge.compiler.FoodKnowledgeBuildCompiler
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AIImportBuildIntegrationTest {

    @Test
    fun buildsFoodKnowledgeWithAIImportFile() {

        val importFile =
            File.createTempFile(
                "ai-knowledge-import",
                ".json"
            )

        importFile.writeText(
            """
            {
              "metadata": {
                "source": "integration-test",
                "generatedBy": "test-ai",
                "generatedAt": "2026-06-28",
                "promptVersion": "test-v1"
              },
              "candidates": [
                {
                  "id": "aepfel integration test",
                  "names": {
                    "canonical": "aepfel integration test",
                    "aliases": ["integration apfel"]
                  },
                  "knowledge": {
                    "carbonImpact": {
                      "reference": "aepfel integration test",
                      "source": "carbon_impact",
                      "value": "LOW"
                    },
                    "glycemicIndex": {
                      "reference": "aepfel integration test",
                      "source": "glycemic_index",
                      "value": "UNKNOWN"
                    }
                  }
                }
              ]
            }
            """.trimIndent()
        )

        FoodKnowledgeBuildCompiler()
            .build(
                importFile = importFile
            )

        val proposedCatalog =
            File("build/generated/foods.proposed.json")

        assertTrue(
            "Expected foods.proposed.json to be generated",
            proposedCatalog.exists()
        )

        val proposedJson =
            proposedCatalog.readText()

        assertTrue(
            "Expected proposed catalog to contain imported item",
            proposedJson.contains("aepfel integration test")
        )

        assertTrue(
            "Expected proposed catalog to contain imported carbon impact value",
            proposedJson.contains("\"value\": \"LOW\"")
        )

        importFile.delete()
    }
}