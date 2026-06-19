package de.shopme.testing.system.tools.knowledge.test

import com.google.gson.Gson
import de.shopme.tools.knowledge.animalwelfare.AnimalWelfareKnowledge
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnimalWelfareKnowledgeTest {

    @Test
    fun `knowledge is not empty`() {

        KnowledgeTestRuntime.start(

            "AnimalWelfareKnowledgeTest"

        )

        try {

            val json =

                checkNotNull(

                    javaClass.classLoader.getResourceAsStream(

                        "knowledge/data/v1/animal_welfare.json"

                    )

                ).bufferedReader().use {

                    it.readText()

                }

            val knowledge =

                Gson().fromJson(

                    json,

                    AnimalWelfareKnowledge::class.java

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

            "AnimalWelfareKnowledgeReferences"

        )

        try {

            val json =

                checkNotNull(

                    javaClass.classLoader.getResourceAsStream(

                        "knowledge/data/v1/animal_welfare.json"

                    )

                ).bufferedReader().use {

                    it.readText()

                }

            val knowledge =

                Gson().fromJson(

                    json,

                    AnimalWelfareKnowledge::class.java

                )

            assertTrue(

                knowledge.entries.containsKey("egg")

            )

            assertTrue(

                knowledge.entries.containsKey("tofu")

            )

            KnowledgeTestRuntime.success(

                "Important food references verified"

            )

        } finally {

            KnowledgeTestRuntime.finish()

        }

    }

}