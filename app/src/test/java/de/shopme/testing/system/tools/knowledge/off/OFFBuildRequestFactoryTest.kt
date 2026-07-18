package de.shopme.testing.system.tools.knowledge.off

import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceType
import de.shopme.tools.knowledge.ai.builder.off.OFFBuildRequestFactory
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OFFBuildRequestFactoryTest {

    @Test
    fun create_buildsRequestFromOpenFoodFactsDump() {
        val file =
            File("../data/raw/openfoodfacts/openfoodfacts-products.jsonl.gz")

        val request =
            OFFBuildRequestFactory.create(
                file = file,
                maxRecords = 10
            )

        assertEquals(
            AIKnowledgeSourceType.OPEN_FOOD_FACTS,
            request.source.type
        )

        assertEquals(
            "Open Food Facts",
            request.source.name
        )

        assertEquals(
            10,
            request.inputs.size
        )

        assertTrue(
            request.inputs.all {
                it.sourceId.isNotBlank()
            }
        )
    }
}