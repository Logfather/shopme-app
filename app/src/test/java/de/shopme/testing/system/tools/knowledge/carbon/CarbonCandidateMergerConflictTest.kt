package de.shopme.testing.system.tools.knowledge.carbon

import de.shopme.tools.knowledge.carbon.merge.CarbonCandidateMerger
import de.shopme.tools.knowledge.carbon.model.CarbonKnowledgeCandidate
import de.shopme.tools.knowledge.source.KnowledgeSourceId
import org.junit.Assert.assertEquals
import org.junit.Test

class CarbonCandidateMergerConflictTest {

    @Test
    fun lastSourceWinsForSameReference() {

        val merger =
            CarbonCandidateMerger()

        val result =
            merger.merge(

                listOf(

                    CarbonKnowledgeCandidate(

                        reference = "apple",

                        kgCo2ePerKg = 0.43,

                        source =
                            KnowledgeSourceId.OPEN_FOOD_FACTS

                    ),

                    CarbonKnowledgeCandidate(

                        reference = "apple",

                        kgCo2ePerKg = 0.41,

                        source =
                            KnowledgeSourceId.AGRIBALYSE

                    )

                )

            )

        val apple =
            requireNotNull(

                result["apple"]

            )

        assertEquals(

            0.41,

            apple.kgCo2ePerKg,

            0.0001

        )

        assertEquals(

            KnowledgeSourceId.AGRIBALYSE,

            apple.source

        )
    }

    @Test
    fun fallsBackToOpenFoodFactsWhenAgribalyseMissing() {

        val merger =
            CarbonCandidateMerger()

        val result =
            merger.merge(

                listOf(

                    CarbonKnowledgeCandidate(

                        reference = "apple",

                        kgCo2ePerKg = 0.43,

                        source =
                            KnowledgeSourceId.OPEN_FOOD_FACTS

                    )

                )

            )

        val apple =
            requireNotNull(

                result["apple"]

            )

        assertEquals(

            0.43,

            apple.kgCo2ePerKg,

            0.0001

        )

        assertEquals(

            KnowledgeSourceId.OPEN_FOOD_FACTS,

            apple.source

        )
    }

    @Test
    fun agribalyseWinsRegardlessOfCandidateOrder() {

        val merger =
            CarbonCandidateMerger()

        val result =
            merger.merge(

                listOf(

                    CarbonKnowledgeCandidate(

                        reference = "apple",

                        kgCo2ePerKg = 0.41,

                        source =
                            KnowledgeSourceId.AGRIBALYSE

                    ),

                    CarbonKnowledgeCandidate(

                        reference = "apple",

                        kgCo2ePerKg = 0.43,

                        source =
                            KnowledgeSourceId.OPEN_FOOD_FACTS

                    )

                )

            )

        val apple =
            requireNotNull(

                result["apple"]

            )

        assertEquals(

            0.41,

            apple.kgCo2ePerKg,

            0.0001

        )

        assertEquals(

            KnowledgeSourceId.AGRIBALYSE,

            apple.source

        )
    }
}