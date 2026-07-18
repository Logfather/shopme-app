package de.shopme.testing.system.tools.knowledge.nutrition.diagnostic

import com.google.gson.GsonBuilder
import de.shopme.testing.system.tools.knowledge.nutrition.diagnostics.NutritionPartialCandidateRelationshipAnalysis
import de.shopme.testing.system.tools.knowledge.nutrition.diagnostics.NutritionUnclassifiedPartialTokenPairAnalysisWriter
import de.shopme.testing.system.tools.knowledge.nutrition.diagnostics.NutritionUnclassifiedPartialTokenPairAnalyzer
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunNutritionUnclassifiedPartialTokenPairAnalysisTest {

    @Test
    fun runNutritionUnclassifiedPartialTokenPairAnalysis() {
        val sourceFile =
            File(
                "../data/generated/knowledge/reports/" +
                        "nutrition.partial-candidate-relationships.json",
            )

        val outputFile =
            File(
                "../data/generated/knowledge/reports/" +
                        "nutrition.unclassified-partial-token-pairs.json",
            )

        require(sourceFile.isFile) {
            "Nutrition partial candidate relationship report does not exist: " +
                    sourceFile.absolutePath
        }

        val gson =
            GsonBuilder()
                .serializeNulls()
                .disableHtmlEscaping()
                .create()

        val source =
            sourceFile
                .reader(Charsets.UTF_8)
                .use { reader ->
                    gson.fromJson(
                        reader,
                        NutritionPartialCandidateRelationshipAnalysis::class.java,
                    )
                }

        val analysis =
            NutritionUnclassifiedPartialTokenPairAnalyzer()
                .analyze(
                    source = source,
                )

        NutritionUnclassifiedPartialTokenPairAnalysisWriter()
            .write(
                analysis = analysis,
                outputFile = outputFile,
            )

        assertEquals(
            expected =
                analysis.sourceUnclassifiedPartialCount,
            actual =
                analysis.analyzedRelationshipCount,
        )

        assertEquals(
            expected =
                analysis.analyzedRelationshipCount,
            actual =
                analysis.singleTokenPairRelationshipCount +
                        analysis.multiTokenRelationshipCount,
        )

        assertEquals(
            expected =
                analysis.tokenPairObservationCount,
            actual =
                analysis.countsByTokenPair.values.sum(),
        )

        assertEquals(
            expected =
                analysis.tokenPairObservationCount,
            actual =
                analysis.countsByNormalizedTokenPair.values.sum(),
        )

        assertTrue(
            analysis.entries.isNotEmpty(),
            "Expected unclassified partial token-pair entries.",
        )

        assertTrue(
            outputFile.isFile,
            "Expected token-pair analysis output file.",
        )

        println()
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        )
        println(
            "NUTRITION UNCLASSIFIED PARTIAL TOKEN PAIRS",
        )
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        )
        println(
            "Source partial candidates : " +
                    analysis.sourcePartialCandidateCount,
        )
        println(
            "Unclassified relationships: " +
                    analysis.sourceUnclassifiedPartialCount,
        )
        println(
            "Analyzed relationships    : " +
                    analysis.analyzedRelationshipCount,
        )
        println(
            "Single-token pairs        : " +
                    analysis.singleTokenPairRelationshipCount,
        )
        println(
            "Multi-token relationships : " +
                    analysis.multiTokenRelationshipCount,
        )
        println(
            "Token-pair observations   : " +
                    analysis.tokenPairObservationCount,
        )
        println(
            "Catalog token counts      : " +
                    analysis.countsByCatalogOnlyToken.size,
        )
        println(
            "Server token counts       : " +
                    analysis.countsByServerOnlyToken.size,
        )
        println(
            "Distinct token pairs      : " +
                    analysis.countsByTokenPair.size,
        )
        println(
            "Normalized token pairs    : " +
                    analysis.countsByNormalizedTokenPair.size,
        )
        println(
            "Catalog token cardinality : " +
                    analysis.countsByCatalogOnlyTokenCount,
        )
        println(
            "Server token cardinality  : " +
                    analysis.countsByServerOnlyTokenCount,
        )
        println(
            "Top catalog-only tokens   : " +
                    analysis.topCatalogOnlyTokens
                        .take(20),
        )
        println(
            "Top server-only tokens    : " +
                    analysis.topServerOnlyTokens
                        .take(20),
        )
        println(
            "Top token pairs           : " +
                    analysis.topTokenPairs
                        .take(20),
        )
        println(
            "Top normalized pairs      : " +
                    analysis.topNormalizedTokenPairs
                        .take(20),
        )
        println(
            "Output                    : " +
                    outputFile.path,
        )
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        )
        println()
    }
}