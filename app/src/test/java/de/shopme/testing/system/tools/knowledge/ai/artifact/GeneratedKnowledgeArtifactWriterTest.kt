package de.shopme.testing.system.tools.knowledge.ai.artifact

import de.shopme.tools.knowledge.ai.builder.artifact.GeneratedKnowledgeArtifactWriter
import de.shopme.tools.knowledge.nutrition.NutritionFacts
import de.shopme.tools.knowledge.nutrition.NutritionFactsKnowledge
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertTrue

class GeneratedKnowledgeArtifactWriterTest {

    @get:Rule
    val temporaryFolder =
        TemporaryFolder()

    private val writer =
        GeneratedKnowledgeArtifactWriter()

    @Test
    fun writesKnowledgeArtifactAsPrettyJson() {
        val artifact =
            NutritionFactsKnowledge(
                entries = mapOf(
                    "banana" to NutritionFacts(
                        calories = 89.0,
                        fat = 0.3,
                        saturatedFat = 0.1,
                        carbohydrates = 22.8,
                        sugar = 12.2,
                        fiber = 2.6,
                        protein = 1.1,
                        salt = 0.0
                    )
                )
            )

        val file =
            writer.write(
                outputDir = temporaryFolder.root,
                fileName = "nutrition.json",
                artifact = artifact
            )

        val json =
            file.readText()

        assertTrue(file.exists())
        assertTrue(json.contains("\"entries\""))
        assertTrue(json.contains("\"banana\""))
        assertTrue(json.contains("\"calories\""))
        assertTrue(json.contains("89.0"))
    }
}