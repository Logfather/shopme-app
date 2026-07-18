package de.shopme.testing.system.tools.knowledge.builder.off

import de.shopme.tools.knowledge.ai.builder.RawKnowledgeInput
import de.shopme.tools.knowledge.ai.builder.off.RawOFFCandidateExtractor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OFFCandidateExtractorTest {

    private val extractor = RawOFFCandidateExtractor()

    @Test
    fun extractReadsSourceId() {
        val input = RawKnowledgeInput(
            sourceId = "off-123",
            fields = emptyMap()
        )

        val data = extractor.extract(input)

        assertEquals("off-123", data.sourceId)
    }

    @Test
    fun extractReadsName() {
        val input = RawKnowledgeInput(
            sourceId = "off-123",
            fields = mapOf(
                "name" to "Apple"
            )
        )

        val data = extractor.extract(input)

        assertEquals("Apple", data.name)
    }

    @Test
    fun extractReturnsNullNameWhenMissing() {
        val input = RawKnowledgeInput(
            sourceId = "off-123",
            fields = emptyMap()
        )

        val data = extractor.extract(input)

        assertNull(data.name)
    }

    @Test
    fun extractReadsNutrition() {

        val nutrition = mapOf(
            "energyKcal100g" to 52.0,
            "fat100g" to 0.2
        )

        val input = RawKnowledgeInput(
            sourceId = "off-123",
            fields = mapOf(
                "nutrition" to nutrition
            )
        )

        val data = extractor.extract(input)

        assertEquals(nutrition, data.nutrition)
    }

    @Test
    fun extractReadsIngredients() {

        val ingredients = listOf(
            "apple",
            "water"
        )

        val input = RawKnowledgeInput(
            sourceId = "off-123",
            fields = mapOf(
                "ingredients" to ingredients
            )
        )

        val data = extractor.extract(input)

        assertEquals(ingredients, data.ingredients)
    }

    @Test
    fun extractReadsTaxonomy() {

        val taxonomy = listOf(
            "Fruit",
            "Apple"
        )

        val input = RawKnowledgeInput(
            sourceId = "off-123",
            fields = mapOf(
                "taxonomy" to taxonomy
            )
        )

        val data = extractor.extract(input)

        assertEquals(taxonomy, data.taxonomy)
    }

    @Test
    fun extractReadsAllergens() {

        val allergens = listOf(
            "milk",
            "soy"
        )

        val input = RawKnowledgeInput(
            sourceId = "off-123",
            fields = mapOf(
                "allergens" to allergens
            )
        )

        val data = extractor.extract(input)

        assertEquals(allergens, data.allergens)
    }

    @Test
    fun extractReadsPackagingProductionAndLocality() {

        val packaging = mapOf(
            "material" to "plastic"
        )

        val production = mapOf(
            "method" to "organic"
        )

        val locality = mapOf(
            "origin" to "Germany"
        )

        val input = RawKnowledgeInput(
            sourceId = "off-123",
            fields = mapOf(
                "packaging" to packaging,
                "production" to production,
                "locality" to locality
            )
        )

        val data = extractor.extract(input)

        assertEquals(packaging, data.packaging)
        assertEquals(production, data.production)
        assertEquals(locality, data.locality)
    }

    @Test
    fun extractReadsGlycemicWaterAndCarbon() {

        val glycemic = mapOf(
            "index" to 38
        )

        val water = mapOf(
            "litersPerKg" to 822
        )

        val carbon = mapOf(
            "kgCo2ePerKg" to 0.4
        )

        val input = RawKnowledgeInput(
            sourceId = "off-123",
            fields = mapOf(
                "glycemic" to glycemic,
                "water" to water,
                "carbon" to carbon
            )
        )

        val data = extractor.extract(input)

        assertEquals(glycemic, data.glycemic)
        assertEquals(water, data.water)
        assertEquals(carbon, data.carbon)
    }

    @Test
    fun extractReadsWaterStressBiodiversityAndPollinator() {

        val waterStress = mapOf(
            "level" to "medium"
        )

        val biodiversity = mapOf(
            "impact" to "low"
        )

        val pollinator = mapOf(
            "dependency" to "high"
        )

        val input = RawKnowledgeInput(
            sourceId = "off-123",
            fields = mapOf(
                "waterStress" to waterStress,
                "biodiversity" to biodiversity,
                "pollinator" to pollinator
            )
        )

        val data = extractor.extract(input)

        assertEquals(waterStress, data.waterStress)
        assertEquals(biodiversity, data.biodiversity)
        assertEquals(pollinator, data.pollinator)
    }

    @Test
    fun extractReadsFairtradeAnimalWelfareAndSeasonality() {

        val fairtrade = mapOf(
            "certified" to true
        )

        val animalWelfare = mapOf(
            "level" to "unknown"
        )

        val seasonality = mapOf(
            "months" to listOf("September", "October")
        )

        val input = RawKnowledgeInput(
            sourceId = "off-123",
            fields = mapOf(
                "fairtrade" to fairtrade,
                "animalWelfare" to animalWelfare,
                "seasonality" to seasonality
            )
        )

        val data = extractor.extract(input)

        assertEquals(fairtrade, data.fairtrade)
        assertEquals(animalWelfare, data.animalWelfare)
        assertEquals(seasonality, data.seasonality)
    }

    @Test
    fun extractReadsFoodMilesRecipeIngredientGraphAndRecipeGraph() {

        val foodMiles = mapOf(
            "distanceKm" to 350
        )

        val recipe = mapOf(
            "id" to "apple_pie"
        )

        val ingredientGraph = mapOf(
            "nodes" to 5
        )

        val recipeGraph = mapOf(
            "nodes" to 2
        )

        val input = RawKnowledgeInput(
            sourceId = "off-123",
            fields = mapOf(
                "foodMiles" to foodMiles,
                "recipe" to recipe,
                "ingredientGraph" to ingredientGraph,
                "recipeGraph" to recipeGraph
            )
        )

        val data = extractor.extract(input)

        assertEquals(foodMiles, data.foodMiles)
        assertEquals(recipe, data.recipe)
        assertEquals(ingredientGraph, data.ingredientGraph)
        assertEquals(recipeGraph, data.recipeGraph)
    }
}