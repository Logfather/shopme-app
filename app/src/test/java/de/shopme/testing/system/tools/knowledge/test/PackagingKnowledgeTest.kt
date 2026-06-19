package de.shopme.testing.system.tools.knowledge.test

import com.google.gson.Gson
import de.shopme.tools.knowledge.packaging.PackagingKnowledge
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PackagingKnowledgeTest {

    @Test
    fun `knowledge is not empty`() {

        KnowledgeTestRuntime.start(

            "PackagingKnowledgeTest"

        )

        try {

            val json =

                checkNotNull(

                    javaClass.classLoader.getResourceAsStream(

                        "knowledge/data/v1/packaging.json"

                    )

                ).bufferedReader().use {

                    it.readText()

                }

            val knowledge =

                Gson().fromJson(

                    json,

                    PackagingKnowledge::class.java

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

            "PackagingKnowledgeReferences"

        )

        try {

            val json =

                checkNotNull(

                    javaClass.classLoader.getResourceAsStream(

                        "knowledge/data/v1/packaging.json"

                    )

                ).bufferedReader().use {

                    it.readText()

                }

            val knowledge =

                Gson().fromJson(

                    json,

                    PackagingKnowledge::class.java

                )

            assertTrue(

                knowledge.entries.containsKey("apple")

            )

            KnowledgeTestRuntime.success(

                "Important food references verified"

            )

        } finally {

            KnowledgeTestRuntime.finish()

        }

    }

}