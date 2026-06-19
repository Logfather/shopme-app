package de.shopme.testing.system.tools.knowledge.test

import com.google.gson.Gson
import de.shopme.tools.knowledge.nutrition.NutritionFactsKnowledge
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NutritionKnowledgeTest {

    @Test
    fun `knowledge is not empty`() {

        KnowledgeTestRuntime.start(

            "NutritionKnowledgeTest"

        )

        try {

            val json =

                checkNotNull(

                    javaClass.classLoader.getResourceAsStream(

                        "knowledge/data/v1/nutrition_facts.json"

                    )

                ).bufferedReader().use {

                    it.readText()

                }

            val knowledge =

                Gson().fromJson(

                    json,

                    NutritionFactsKnowledge::class.java

                )


            KnowledgeTestRuntime.statistic(

                "Food references",

                knowledge.entries.size

            )

            assertFalse(

                knowledge.entries.isEmpty()

            )

            KnowledgeTestRuntime.success(

                "Knowledge loaded successfully"

            )

        } finally {

            KnowledgeTestRuntime.finish()

        }

    }

    @Test
    fun `knowledge contains important food references`() {

        KnowledgeTestRuntime.start(

            "NutritionKnowledgeReferences"

        )

        try {

            val json =

                checkNotNull(

                    javaClass.classLoader.getResourceAsStream(

                        "knowledge/data/v1/nutrition_facts.json"

                    )

                ).bufferedReader().use {

                    it.readText()

                }

            val knowledge =

                Gson().fromJson(

                    json,

                    NutritionFactsKnowledge::class.java

                )

            assertTrue(

                knowledge.entries.containsKey("apple")

            )

            assertTrue(

                knowledge.entries.containsKey("banana")

            )

            KnowledgeTestRuntime.success(

                "Important food references verified"

            )

        } finally {

            KnowledgeTestRuntime.finish()

        }

    }

}