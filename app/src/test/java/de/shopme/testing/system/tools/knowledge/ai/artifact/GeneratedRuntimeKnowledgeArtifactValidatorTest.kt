package de.shopme.testing.system.tools.knowledge.ai.artifact

import de.shopme.tools.knowledge.ai.builder.artifact.GeneratedRuntimeKnowledgeArtifactValidator
import de.shopme.tools.knowledge.environment.EnvironmentalImpact
import de.shopme.tools.knowledge.environment.EnvironmentalImpactKnowledge
import de.shopme.tools.knowledge.nutrition.NutritionFacts
import de.shopme.tools.knowledge.nutrition.NutritionFactsKnowledge
import kotlin.test.Test
import kotlin.test.assertFailsWith

class GeneratedRuntimeKnowledgeArtifactValidatorTest {

    private val validator =
        GeneratedRuntimeKnowledgeArtifactValidator()

    @Test
    fun acceptsValidNutritionArtifact() {
        validator.validate(
            artifactName = "nutrition.json",
            artifact = NutritionFactsKnowledge(
                entries = mapOf(
                    "apple" to nutrition(),
                    "banana" to nutrition()
                )
            )
        )
    }

    @Test
    fun acceptsValidEnvironmentalImpactArtifact() {
        validator.validate(
            artifactName = "environmental_impact.json",
            artifact = EnvironmentalImpactKnowledge(
                entries = mapOf(
                    "apple" to environmentalImpact(),
                    "banana" to environmentalImpact()
                )
            )
        )
    }

    @Test
    fun rejectsEmptyArtifact() {
        assertFailsWith<IllegalArgumentException> {
            validator.validate(
                artifactName = "nutrition.json",
                artifact = NutritionFactsKnowledge(
                    entries = emptyMap()
                )
            )
        }
    }

    @Test
    fun rejectsBlankKeys() {
        assertFailsWith<IllegalArgumentException> {
            validator.validate(
                artifactName = "nutrition.json",
                artifact = NutritionFactsKnowledge(
                    entries = mapOf(
                        "" to nutrition()
                    )
                )
            )
        }
    }

    @Test
    fun rejectsUntrimmedKeys() {
        assertFailsWith<IllegalArgumentException> {
            validator.validate(
                artifactName = "nutrition.json",
                artifact = NutritionFactsKnowledge(
                    entries = mapOf(
                        " banana " to nutrition()
                    )
                )
            )
        }
    }

    @Test
    fun rejectsUppercaseKeys() {
        assertFailsWith<IllegalArgumentException> {
            validator.validate(
                artifactName = "nutrition.json",
                artifact = NutritionFactsKnowledge(
                    entries = mapOf(
                        "Banana" to nutrition()
                    )
                )
            )
        }
    }

    @Test
    fun rejectsUnsortedKeys() {
        assertFailsWith<IllegalArgumentException> {
            validator.validate(
                artifactName = "nutrition.json",
                artifact = NutritionFactsKnowledge(
                    entries = linkedMapOf(
                        "banana" to nutrition(),
                        "apple" to nutrition()
                    )
                )
            )
        }
    }

    private fun nutrition(): NutritionFacts {
        return NutritionFacts(
            calories = 1.0,
            fat = 1.0,
            saturatedFat = 1.0,
            carbohydrates = 1.0,
            sugar = 1.0,
            fiber = 1.0,
            protein = 1.0,
            salt = 1.0
        )
    }

    private fun environmentalImpact(): EnvironmentalImpact {
        return EnvironmentalImpact(
            environmentScoreMptPerKg = 1.0,
            climateKgCo2EqPerKg = 1.0,
            landUsePtPerKg = 1.0,
            waterDeprivationM3PerKg = 1.0
        )
    }
}