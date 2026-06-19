package de.shopme.testing.system.tools.knowledge.test

import com.google.gson.Gson
import de.shopme.tools.knowledge.waterfootprint.WaterKnowledge
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WaterKnowledgeTest {

    @Test
    fun `knowledge is not empty`() {

        KnowledgeTestRuntime.start(

            "WaterKnowledgeTest"

        )

        try {

            val json =

                checkNotNull(

                    javaClass.classLoader.getResourceAsStream(

                        "knowledge/data/v1/water_footprint.json"

                    )

                ).bufferedReader().use {

                    it.readText()

                }

            val knowledge =

                Gson().fromJson(

                    json,

                    WaterKnowledge::class.java

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

            "WaterKnowledgeReferences"

        )

        try {

            val json =

                checkNotNull(

                    javaClass.classLoader.getResourceAsStream(

                        "knowledge/data/v1/water_footprint.json"

                    )

                ).bufferedReader().use {

                    it.readText()

                }

            val knowledge =

                Gson().fromJson(

                    json,

                    WaterKnowledge::class.java

                )

            assertTrue(

                knowledge.entries.containsKey("apple")

            )

            assertTrue(

                knowledge.entries.containsKey("beef")

            )

            KnowledgeTestRuntime.success(

                "Important food references verified"

            )

        } finally {

            KnowledgeTestRuntime.finish()

        }

    }

}