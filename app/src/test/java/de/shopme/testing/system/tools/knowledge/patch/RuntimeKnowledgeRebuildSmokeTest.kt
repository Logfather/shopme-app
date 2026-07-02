package de.shopme.testing.system.tools.knowledge.patch

import de.shopme.tools.knowledge.compiler.CreateFoodKnowledge
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RuntimeKnowledgeRebuildSmokeTest {

    @Test
    fun rebuildRuntimeKnowledgeFromGeneratedFoodsJson() {

        val generatedFoods =
            File("data/generated/foods.json")

        assertTrue(
            "Expected generated foods.json to exist before rebuild.",
            generatedFoods.exists()
        )

        CreateFoodKnowledge.main(emptyArray())

        val rebuiltFoods =
            File("data/generated/foods.json")

        val nutrition =
            File("data/generated/nutrition.json")

        assertTrue(
            "Expected rebuilt foods.json to exist.",
            rebuiltFoods.exists()
        )

        assertTrue(
            "Expected nutrition.json to be generated.",
            nutrition.exists()
        )

        assertTrue(
            "Expected rebuilt foods.json to be non-empty.",
            rebuiltFoods.length() > 0
        )

        assertTrue(
            "Expected nutrition.json to be non-empty.",
            nutrition.length() > 0
        )
    }
}