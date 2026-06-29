package de.shopme.testing.system.tools.knowledge.carbon

import de.shopme.tools.knowledge.carbon.merge.CarbonCandidateMergeReportBuilder
import de.shopme.tools.knowledge.carbon.merge.CarbonCandidateMerger
import de.shopme.tools.knowledge.carbon.model.CarbonKnowledgeCandidate
import de.shopme.tools.knowledge.source.KnowledgeSourceId
import org.junit.Assert.assertEquals
import org.junit.Test

class CarbonCandidateMergeReportBuilderTest {

    @Test
    fun buildsReportWithConflicts() {

        val candidates =
            listOf(

                CarbonKnowledgeCandidate(
                    reference = "apple",
                    kgCo2ePerKg = 0.43,
                    source = KnowledgeSourceId.OPEN_FOOD_FACTS
                ),

                CarbonKnowledgeCandidate(
                    reference = "apple",
                    kgCo2ePerKg = 0.41,
                    source = KnowledgeSourceId.AGRIBALYSE
                ),

                CarbonKnowledgeCandidate(
                    reference = "banana",
                    kgCo2ePerKg = 0.91,
                    source = KnowledgeSourceId.OPEN_FOOD_FACTS
                )
            )

        val merged =
            CarbonCandidateMerger()
                .merge(
                    candidates
                )

        val report =
            CarbonCandidateMergeReportBuilder()
                .build(
                    candidates = candidates,
                    merged = merged
                )

        assertEquals(
            3,
            report.totalCandidates
        )

        assertEquals(
            2,
            report.mergedReferences
        )

        assertEquals(
            1,
            report.conflicts.size
        )

        val conflict =
            report.conflicts.first()

        assertEquals(
            "apple",
            conflict.reference
        )

        assertEquals(
            2,
            conflict.candidates.size
        )
    }
}