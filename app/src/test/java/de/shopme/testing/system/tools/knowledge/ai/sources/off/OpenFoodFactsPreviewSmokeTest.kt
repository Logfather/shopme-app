package de.shopme.testing.system.tools.knowledge.ai.sources.off

import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceType
import de.shopme.tools.knowledge.ai.sources.off.OFFAIImportAdapter
import de.shopme.tools.knowledge.ai.sources.off.OFFJsonlPreviewReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

class OpenFoodFactsPreviewSmokeTest {

    @Test
    fun readPreviewFileAndAdaptToAIKnowledgeBuildRequest() {

        val file =
            File(
                "data/preview/openfoodfacts/off-products-preview"
            )

        assumeTrue(
            "OFF preview file not found: ${file.absolutePath}",
            file.exists()
        )

        val products =
            OFFJsonlPreviewReader()
                .read(
                    file = file,
                    limit = 10
                )

        val request =
            OFFAIImportAdapter()
                .adapt(products)

        assertEquals(
            AIKnowledgeSourceType.OPEN_FOOD_FACTS,
            request.source.type
        )

        assertEquals(
            "Open Food Facts",
            request.source.name
        )

        assertEquals(
            products.size,
            request.inputs.size
        )

        assertTrue(
            request.inputs.isNotEmpty()
        )

        assertTrue(
            request.inputs.all {
                it.sourceId.isNotBlank()
            }
        )

        assertTrue(
            request.inputs.all {
                it.fields.isNotEmpty()
            }
        )
    }
}