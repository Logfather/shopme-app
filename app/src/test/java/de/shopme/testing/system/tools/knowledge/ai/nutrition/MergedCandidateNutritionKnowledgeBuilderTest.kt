package de.shopme.testing.system.tools.knowledge.ai.nutrition

import de.shopme.tools.knowledge.ai.builder.nutrition.MergedCandidateNutritionKnowledgeBuilder
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import de.shopme.tools.knowledge.nutrition.NutritionFacts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

class MergedCandidateNutritionKnowledgeBuilderTest {

    private val builder = MergedCandidateNutritionKnowledgeBuilder()

    @Test
    fun buildsNutritionKnowledgeFromMergedCandidates() {
        val candidate = CanonicalKnowledgeCandidate(
            canonicalId = "banana",
            aliases = setOf("banane"),
            matchAliases = setOf("fruit"),
            dimensions = listOf(
                KnowledgeDimensionCandidate(
                    dimension = KnowledgeDimensionCandidateType.NUTRITION,
                    payload = NutritionFacts(
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
            ),
            metadata = testMetadata()
        )

        val knowledge = builder.build(listOf(candidate))

        val banana = knowledge.entries["banana"]

        assertNotNull(banana)
        assertEquals(89.0, banana!!.calories, 0.0)
        assertEquals(0.3, banana.fat, 0.0)
        assertEquals(12.2, banana.sugar, 0.0)

        assertFalse(knowledge.entries.containsKey("banane"))
        assertFalse(knowledge.entries.containsKey("fruit"))
    }

    @Test
    fun ignoresCandidatesWithoutNutrition() {
        val candidate = CanonicalKnowledgeCandidate(
            canonicalId = "apple",
            aliases = setOf("apfel"),
            matchAliases = setOf("fruit"),
            dimensions = emptyList(),
            metadata = testMetadata()
        )

        val knowledge = builder.build(listOf(candidate))

        assertEquals(emptyMap<String, NutritionFacts>(), knowledge.entries)
    }

    @Test
    fun producesStableSortedEntries() {
        val banana = candidateWithCalories("banana", 89.0)
        val apple = candidateWithCalories("apple", 52.0)

        val knowledge = builder.build(listOf(banana, apple))

        assertEquals(listOf("apple", "banana"), knowledge.entries.keys.toList())
    }

    private fun candidateWithCalories(
        canonicalId: String,
        calories: Double
    ): CanonicalKnowledgeCandidate {
        return CanonicalKnowledgeCandidate(
            canonicalId = canonicalId,
            aliases = emptySet(),
            matchAliases = emptySet(),
            dimensions = listOf(
                KnowledgeDimensionCandidate(
                    dimension = KnowledgeDimensionCandidateType.NUTRITION,
                    payload = NutritionFacts(
                        calories = calories,
                        fat = 0.0,
                        saturatedFat = 0.0,
                        carbohydrates = 0.0,
                        sugar = 0.0,
                        fiber = 0.0,
                        protein = 0.0,
                        salt = 0.0
                    )
                )
            ),
            metadata = testMetadata()
        )
    }
    private fun testMetadata(): CandidateMetadata {
        return CandidateMetadata(
            source = "test",
            sourceId = "test",
            version = "test",
            confidence = 1.0
        )
    }
}