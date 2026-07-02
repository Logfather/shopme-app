package de.shopme.testing.system.tools.knowledge.builder.off

import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildRequest
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceInfo
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceType
import de.shopme.tools.knowledge.ai.builder.RawKnowledgeInput
import de.shopme.tools.knowledge.ai.builder.off.DeterministicOFFCandidateBuilder
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import org.junit.Assert.assertEquals
import org.junit.Test

class DeterministicOFFCandidateBuilderTest {

    private val builder = DeterministicOFFCandidateBuilder()

    @Test
    fun buildCreatesCandidateFromValidInput() {
        val request = request(
            input(
                sourceId = "off-123",
                fields = mapOf(
                    "name" to "Apple"
                )
            )
        )

        val result = builder.build(request)

        assertEquals(1, result.candidates.size)

        val candidate = result.candidates.first()

        assertEquals("off-123", candidate.canonicalId)
        assertEquals(emptyList<KnowledgeDimensionCandidate>(), candidate.dimensions)
        assertEquals("Open Food Facts", candidate.metadata.source)
        assertEquals("off-123", candidate.metadata.sourceId)
        assertEquals(1.0, candidate.metadata.confidence, 0.0)
        assertEquals("preview", candidate.metadata.version)
        assertEquals(setOf("Apple"), candidate.aliases)
    }

    @Test
    fun buildIgnoresInputWithoutSourceId() {
        val request = request(
            input(
                sourceId = "",
                fields = mapOf(
                    "name" to "Apple"
                )
            )
        )

        val result = builder.build(request)

        assertEquals(0, result.candidates.size)
    }

    @Test
    fun buildIsDeterministic() {
        val request = request(
            input(
                sourceId = "off-123",
                fields = mapOf(
                    "name" to "Apple"
                )
            )
        )

        val first = builder.build(request)
        val second = builder.build(request)

        assertEquals(first, second)
    }

    @Test
    fun buildIgnoresInputWithoutName() {
        val request = request(
            input(
                sourceId = "off-123",
                fields = mapOf(
                    "name" to ""
                )
            )
        )

        val result = builder.build(request)

        assertEquals(0, result.candidates.size)
    }

    @Test
    fun buildCreatesNutritionDimension() {

        val nutrition = mapOf(
            "energyKcal100g" to 52.0
        )

        val request = request(
            input(
                sourceId = "off-123",
                fields = mapOf(
                    "name" to "Apple",
                    "nutrition" to nutrition
                )
            )
        )

        val result = builder.build(request)

        assertEquals(1, result.candidates.size)

        val candidate = result.candidates.first()

        assertEquals(1, candidate.dimensions.size)

        val dimension = candidate.dimensions.first()

        assertEquals(
            KnowledgeDimensionCandidateType.NUTRITION,
            dimension.dimension
        )

        assertEquals(
            nutrition,
            dimension.payload
        )
    }

    @Test
    fun buildCreatesIngredientsDimension() {

        val ingredients = listOf(
            "apple",
            "water"
        )

        val request = request(
            input(
                sourceId = "off-123",
                fields = mapOf(
                    "name" to "Apple Juice",
                    "ingredients" to ingredients
                )
            )
        )

        val result = builder.build(request)

        assertEquals(1, result.candidates.size)

        val candidate = result.candidates.first()

        assertEquals(1, candidate.dimensions.size)

        val dimension = candidate.dimensions.first()

        assertEquals(
            KnowledgeDimensionCandidateType.INGREDIENTS,
            dimension.dimension
        )

        assertEquals(
            ingredients,
            dimension.payload
        )
    }

    @Test
    fun buildCreatesTaxonomyDimension() {

        val taxonomy = listOf(
            "Fruit",
            "Apple"
        )

        val request = request(
            input(
                sourceId = "off-123",
                fields = mapOf(
                    "name" to "Apple",
                    "taxonomy" to taxonomy
                )
            )
        )

        val result = builder.build(request)

        assertEquals(1, result.candidates.size)

        val candidate = result.candidates.first()

        assertEquals(1, candidate.dimensions.size)

        val dimension = candidate.dimensions.first()

        assertEquals(
            KnowledgeDimensionCandidateType.TAXONOMY,
            dimension.dimension
        )

        assertEquals(
            taxonomy,
            dimension.payload
        )
    }

    @Test
    fun buildCreatesAllergensDimension() {

        val allergens = listOf(
            "milk",
            "soy"
        )

        val request = request(
            input(
                sourceId = "off-123",
                fields = mapOf(
                    "name" to "Chocolate",
                    "allergens" to allergens
                )
            )
        )

        val result = builder.build(request)

        assertEquals(1, result.candidates.size)

        val candidate = result.candidates.first()

        assertEquals(1, candidate.dimensions.size)

        val dimension = candidate.dimensions.first()

        assertEquals(
            KnowledgeDimensionCandidateType.ALLERGENS,
            dimension.dimension
        )

        assertEquals(
            allergens,
            dimension.payload
        )
    }

    @Test
    fun buildCreatesPackagingProductionAndLocalityDimensions() {

        val packaging = mapOf(
            "material" to "plastic"
        )

        val production = mapOf(
            "method" to "organic"
        )

        val locality = mapOf(
            "origin" to "Germany"
        )

        val request = request(
            input(
                sourceId = "off-123",
                fields = mapOf(
                    "name" to "Apple Juice",
                    "packaging" to packaging,
                    "production" to production,
                    "locality" to locality
                )
            )
        )

        val result = builder.build(request)

        assertEquals(1, result.candidates.size)

        val candidate = result.candidates.first()

        assertEquals(3, candidate.dimensions.size)

        assertEquals(
            packaging,
            candidate.dimensions.first {
                it.dimension == KnowledgeDimensionCandidateType.PACKAGING
            }.payload
        )

        assertEquals(
            production,
            candidate.dimensions.first {
                it.dimension == KnowledgeDimensionCandidateType.PRODUCTION
            }.payload
        )

        assertEquals(
            locality,
            candidate.dimensions.first {
                it.dimension == KnowledgeDimensionCandidateType.LOCALITY
            }.payload
        )
    }

    @Test
    fun buildCreatesGlycemicWaterAndCarbonDimensions() {

        val glycemic = mapOf(
            "index" to 38
        )

        val water = mapOf(
            "litersPerKg" to 822
        )

        val carbon = mapOf(
            "kgCo2ePerKg" to 0.4
        )

        val request = request(
            input(
                sourceId = "off-123",
                fields = mapOf(
                    "name" to "Apple",
                    "glycemic" to glycemic,
                    "water" to water,
                    "carbon" to carbon
                )
            )
        )

        val result = builder.build(request)

        assertEquals(1, result.candidates.size)

        val candidate = result.candidates.first()

        assertEquals(3, candidate.dimensions.size)

        assertEquals(
            glycemic,
            candidate.dimensions.first {
                it.dimension == KnowledgeDimensionCandidateType.GLYCEMIC
            }.payload
        )

        assertEquals(
            water,
            candidate.dimensions.first {
                it.dimension == KnowledgeDimensionCandidateType.WATER
            }.payload
        )

        assertEquals(
            carbon,
            candidate.dimensions.first {
                it.dimension == KnowledgeDimensionCandidateType.CARBON
            }.payload
        )
    }

    private fun request(
        vararg inputs: RawKnowledgeInput
    ): AIKnowledgeBuildRequest {
        return AIKnowledgeBuildRequest(
            source = AIKnowledgeSourceInfo(
                type = AIKnowledgeSourceType.OPEN_FOOD_FACTS,
                name = "Open Food Facts",
                version = "preview"
            ),
            inputs = inputs.toList()
        )
    }

    @Test
    fun buildCreatesWaterStressBiodiversityAndPollinatorDimensions() {

        val waterStress = mapOf(
            "level" to "medium"
        )

        val biodiversity = mapOf(
            "impact" to "low"
        )

        val pollinator = mapOf(
            "dependency" to "high"
        )

        val request = request(
            input(
                sourceId = "off-123",
                fields = mapOf(
                    "name" to "Apple",
                    "waterStress" to waterStress,
                    "biodiversity" to biodiversity,
                    "pollinator" to pollinator
                )
            )
        )

        val result = builder.build(request)

        assertEquals(1, result.candidates.size)

        val candidate = result.candidates.first()

        assertEquals(3, candidate.dimensions.size)

        assertEquals(
            waterStress,
            candidate.dimensions.first {
                it.dimension == KnowledgeDimensionCandidateType.WATER_STRESS
            }.payload
        )

        assertEquals(
            biodiversity,
            candidate.dimensions.first {
                it.dimension == KnowledgeDimensionCandidateType.BIODIVERSITY
            }.payload
        )

        assertEquals(
            pollinator,
            candidate.dimensions.first {
                it.dimension == KnowledgeDimensionCandidateType.POLLINATOR
            }.payload
        )
    }

    @Test
    fun buildCreatesFairtradeAnimalWelfareAndSeasonalityDimensions() {

        val fairtrade = mapOf(
            "certified" to true
        )

        val animalWelfare = mapOf(
            "level" to "unknown"
        )

        val seasonality = mapOf(
            "months" to listOf("September", "October")
        )

        val request = request(
            input(
                sourceId = "off-123",
                fields = mapOf(
                    "name" to "Apple",
                    "fairtrade" to fairtrade,
                    "animalWelfare" to animalWelfare,
                    "seasonality" to seasonality
                )
            )
        )

        val result = builder.build(request)

        assertEquals(1, result.candidates.size)

        val candidate = result.candidates.first()

        assertEquals(3, candidate.dimensions.size)

        assertEquals(
            fairtrade,
            candidate.dimensions.first {
                it.dimension == KnowledgeDimensionCandidateType.FAIRTRADE
            }.payload
        )

        assertEquals(
            animalWelfare,
            candidate.dimensions.first {
                it.dimension == KnowledgeDimensionCandidateType.ANIMAL_WELFARE
            }.payload
        )

        assertEquals(
            seasonality,
            candidate.dimensions.first {
                it.dimension == KnowledgeDimensionCandidateType.SEASONALITY
            }.payload
        )
    }

    @Test
    fun buildCreatesFoodMilesRecipeIngredientGraphAndRecipeGraphDimensions() {

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

        val request = request(
            input(
                sourceId = "off-123",
                fields = mapOf(
                    "name" to "Apple Pie",
                    "foodMiles" to foodMiles,
                    "recipe" to recipe,
                    "ingredientGraph" to ingredientGraph,
                    "recipeGraph" to recipeGraph
                )
            )
        )

        val result = builder.build(request)

        assertEquals(1, result.candidates.size)

        val candidate = result.candidates.first()

        assertEquals(4, candidate.dimensions.size)

        assertEquals(
            foodMiles,
            candidate.dimensions.first {
                it.dimension == KnowledgeDimensionCandidateType.FOOD_MILES
            }.payload
        )

        assertEquals(
            recipe,
            candidate.dimensions.first {
                it.dimension == KnowledgeDimensionCandidateType.RECIPE
            }.payload
        )

        assertEquals(
            ingredientGraph,
            candidate.dimensions.first {
                it.dimension == KnowledgeDimensionCandidateType.INGREDIENT_GRAPH
            }.payload
        )

        assertEquals(
            recipeGraph,
            candidate.dimensions.first {
                it.dimension == KnowledgeDimensionCandidateType.RECIPE_GRAPH
            }.payload
        )
    }

    private fun input(
        sourceId: String,
        fields: Map<String, Any?>
    ): RawKnowledgeInput {
        return RawKnowledgeInput(
            sourceId = sourceId,
            fields = fields
        )
    }
}