package de.shopme.testing.system.tools.knowledge.carbon

import de.shopme.tools.knowledge.carbon.merge.CarbonCandidateMergeReport
import de.shopme.tools.knowledge.carbon.merge.CarbonMergeConflict
import de.shopme.tools.knowledge.carbon.merge.CarbonMergeReportPrinter
import de.shopme.tools.knowledge.carbon.model.CarbonKnowledgeCandidate
import de.shopme.tools.knowledge.source.KnowledgeSourceId
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class CarbonMergeReportPrinterTest {

    @Test
    fun printsConflictReport() {

        val report =
            CarbonCandidateMergeReport(

                totalCandidates = 2,

                mergedReferences = 1,

                conflicts = listOf(

                    CarbonMergeConflict(

                        reference = "apple",

                        candidates = listOf(

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

                )

            )

        val originalOut =
            System.out

        val output =
            ByteArrayOutputStream()

        System.setOut(
            PrintStream(output)
        )

        try {

            CarbonMergeReportPrinter()
                .print(
                    report
                )

        } finally {

            System.setOut(
                originalOut
            )

        }

        val text =
            output.toString()

        assertTrue(
            text.contains(
                "CARBON MERGE REPORT"
            )
        )

        assertTrue(
            text.contains(
                "apple"
            )
        )

        assertTrue(
            text.contains(
                "OPEN_FOOD_FACTS"
            )
        )

        assertTrue(
            text.contains(
                "AGRIBALYSE"
            )
        )

        assertTrue(
            text.contains(
                "0.43"
            )
        )

        assertTrue(
            text.contains(
                "0.41"
            )
        )
    }
}