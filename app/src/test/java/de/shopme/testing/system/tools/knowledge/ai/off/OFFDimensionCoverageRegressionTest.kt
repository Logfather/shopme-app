package de.shopme.testing.system.tools.knowledge.ai.off

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.ai.builder.DefaultAIKnowledgeBuilderPipelineFactory
import de.shopme.tools.knowledge.ai.sources.off.OFFAIImportAdapter
import de.shopme.tools.knowledge.ai.sources.off.OFFRawProduct
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.File

class OFFDimensionCoverageRegressionTest {

    @Test
    fun mapsOffProductDimensionsIntoCatalogKnowledgeReferences() {

        val catalogFile =
            File.createTempFile(
                "off-dimension-coverage-regression",
                ".json"
            )

        try {
            catalogFile.writeText("[]")

            val product =
                OFFRawProduct(
                    code = "00000758",
                    productName = "Test Food",
                    ingredientsText = "Milk, sugar",
                    categories = "Snacks",
                    labels = "Fairtrade, Organic, Free range",
                    countries = "Germany",
                    origins = "France",
                    allergens = "en:milk",
                    packaging = "plastic tray",
                    manufacturingPlaces = "Berlin",
                    novaGroup = 4,
                    energyKcal100g = 100.0
                )

            val request =
                OFFAIImportAdapter()
                    .adapt(listOf(product))

            DefaultAIKnowledgeBuilderPipelineFactory
                .create(catalogFile = catalogFile)
                .run(request)

            val catalog: List<CatalogItem> =
                Gson().fromJson(
                    catalogFile.readText(),
                    object : TypeToken<List<CatalogItem>>() {}.type
                )

            val item =
                catalog.single()

            assertEquals("00000758", item.normalized)

            assertNotNull(item.knowledge?.nutrition)
            assertNotNull(item.knowledge?.ingredients)
            assertNotNull(item.knowledge?.taxonomy)
            assertNotNull(item.knowledge?.allergens)
            assertNotNull(item.knowledge?.packaging)
            assertNotNull(item.knowledge?.fairTrade)
            assertNotNull(item.knowledge?.animalWelfare)
            assertNotNull(item.knowledge?.locality)
            assertNotNull(item.knowledge?.production)
            assertNotNull(item.knowledge?.processing)

            assertEquals("00000758", item.knowledge?.nutrition?.reference)
            assertEquals("Open Food Facts", item.knowledge?.nutrition?.source)

        } finally {
            catalogFile.delete()
        }
    }
}