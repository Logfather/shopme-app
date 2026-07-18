package de.shopme.testing.system.tools.knowledge.nutrition.diagnostic

import com.google.gson.GsonBuilder
import de.shopme.testing.system.tools.knowledge.nutrition.diagnostics.NutritionPartialCandidateRelationshipAnalysisWriter
import de.shopme.testing.system.tools.knowledge.nutrition.diagnostics.NutritionPartialCandidateRelationshipAnalyzer
import de.shopme.testing.system.tools.knowledge.nutrition.diagnostics.NutritionScoreClusterCandidateAnalysis
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunNutritionPartialCandidateRelationshipAnalysisTest {

    @Test
    fun runNutritionPartialCandidateRelationshipAnalysis() {
        val sourceFile =
            File(
                "../data/generated/knowledge/reports/" +
                        "nutrition.score-cluster-candidate-analysis.json",
            )

        val outputFile =
            File(
                "../data/generated/knowledge/reports/" +
                        "nutrition.partial-candidate-relationships.json",
            )

        require(sourceFile.isFile) {
            "Nutrition score-cluster candidate analysis does not exist: " +
                    sourceFile.absolutePath
        }

        val gson =
            GsonBuilder()
                .serializeNulls()
                .disableHtmlEscaping()
                .create()

        val source =
            sourceFile
                .reader()
                .use { reader ->
                    gson.fromJson(
                        reader,
                        NutritionScoreClusterCandidateAnalysis::class.java,
                    )
                }

        val analysis =
            NutritionPartialCandidateRelationshipAnalyzer()
                .analyze(
                    source = source,
                )

        NutritionPartialCandidateRelationshipAnalysisWriter()
            .write(
                analysis = analysis,
                outputFile = outputFile,
            )

        assertEquals(
            expected =
                source.countsByContainmentType[
                    de.shopme.testing.system.tools.knowledge.nutrition
                        .diagnostics
                        .NutritionScoreClusterContainmentType
                        .PARTIAL
                ],
            actual =
                analysis.partialCandidateCount,
        )

        assertEquals(
            expected = analysis.partialCandidateCount,
            actual = analysis.classifiedCandidateCount,
        )

        assertEquals(
            expected = analysis.classifiedCandidateCount,
            actual =
                analysis.countsByPrimaryRelationshipType
                    .values
                    .sum(),
        )

        assertTrue(
            analysis.entries.isNotEmpty(),
            "Expected partial candidate relationship entries.",
        )

        assertTrue(
            outputFile.isFile,
            "Expected partial candidate relationship output file.",
        )

        println()
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        )
        println(
            "NUTRITION PARTIAL CANDIDATE RELATIONSHIPS",
        )
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        )
        println(
            "Source entries          : " +
                    analysis.sourceEntryCount,
        )
        println(
            "Source candidates       : " +
                    analysis.sourceCandidateCount,
        )
        println(
            "Partial candidates      : " +
                    analysis.partialCandidateCount,
        )
        println(
            "Classified candidates   : " +
                    analysis.classifiedCandidateCount,
        )
        println(
            "Primary relationships   : " +
                    analysis.countsByPrimaryRelationshipType,
        )
        println(
            "Detected relationships  : " +
                    analysis.countsByDetectedRelationshipType,
        )
        println(
            "Catalog-only tokens     : " +
                    analysis.countsByCatalogOnlyTokenCount,
        )
        println(
            "Server-only tokens      : " +
                    analysis.countsByServerOnlyTokenCount,
        )
        println(
            "Output                  : " +
                    outputFile.path,
        )
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        )
        println()
    }
}