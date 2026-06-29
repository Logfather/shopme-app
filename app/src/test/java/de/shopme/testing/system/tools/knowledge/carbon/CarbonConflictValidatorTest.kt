package de.shopme.testing.system.tools.knowledge.carbon

import de.shopme.tools.knowledge.carbon.merge.CarbonCandidateMergeReport
import de.shopme.tools.knowledge.carbon.merge.CarbonMergeConflict
import de.shopme.tools.knowledge.carbon.model.CarbonKnowledgeCandidate
import de.shopme.tools.knowledge.carbon.validation.CarbonConflictValidator
import de.shopme.tools.knowledge.source.KnowledgeSourceId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CarbonConflictValidatorTest {

    @Test
    fun ignoresConflictsBelowFivePercent() {

        val report =
            CarbonCandidateMergeReport(

                totalCandidates = 2,

                mergedReferences = 1,

                conflicts =
                    listOf(

                        CarbonMergeConflict(

                            reference = "apple",

                            candidates =
                                listOf(

                                    CarbonKnowledgeCandidate(

                                        reference = "apple",

                                        kgCo2ePerKg = 1.00,

                                        source =
                                            KnowledgeSourceId.OPEN_FOOD_FACTS

                                    ),

                                    CarbonKnowledgeCandidate(

                                        reference = "apple",

                                        kgCo2ePerKg = 1.04,

                                        source =
                                            KnowledgeSourceId.AGRIBALYSE

                                    )
                                )
                        )
                    )
            )

        val result =
            CarbonConflictValidator()
                .validate(report)

        assertTrue(
            result.warnings.isEmpty()
        )
    }

    @Test
    fun createsWarningForConflictsAboveFivePercent() {

        val report =
            CarbonCandidateMergeReport(

                totalCandidates = 2,

                mergedReferences = 1,

                conflicts =
                    listOf(

                        CarbonMergeConflict(

                            reference = "apple",

                            candidates =
                                listOf(

                                    CarbonKnowledgeCandidate(

                                        reference = "apple",

                                        kgCo2ePerKg = 1.00,

                                        source =
                                            KnowledgeSourceId.OPEN_FOOD_FACTS

                                    ),

                                    CarbonKnowledgeCandidate(

                                        reference = "apple",

                                        kgCo2ePerKg = 1.20,

                                        source =
                                            KnowledgeSourceId.AGRIBALYSE

                                    )
                                )
                        )
                    )
            )

        val result =
            CarbonConflictValidator()
                .validate(report)

        assertEquals(
            1,
            result.warnings.size
        )

        val warning =
            result.warnings.first()

        assertEquals(
            "apple",
            warning.reference
        )

        assertEquals(
            1.00,
            warning.minKgCo2ePerKg,
            0.0001
        )

        assertEquals(
            1.20,
            warning.maxKgCo2ePerKg,
            0.0001
        )
    }
}