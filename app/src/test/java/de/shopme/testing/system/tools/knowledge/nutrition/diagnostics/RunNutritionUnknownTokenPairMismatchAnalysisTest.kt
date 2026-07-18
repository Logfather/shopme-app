package de.shopme.testing.system.tools.knowledge.nutrition.diagnostic

import com.google.gson.GsonBuilder
import de.shopme.testing.system.tools.knowledge.nutrition.diagnostics.NutritionUnclassifiedTokenPairMismatchClassification
import de.shopme.testing.system.tools.knowledge.nutrition.diagnostics.NutritionUnknownTokenPairMismatchAnalysisWriter
import de.shopme.testing.system.tools.knowledge.nutrition.diagnostics.NutritionUnknownTokenPairMismatchAnalyzer
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunNutritionUnknownTokenPairMismatchAnalysisTest {

    @Test
    fun runNutritionUnknownTokenPairMismatchAnalysis() {
        val sourceFile =
            File(
                "../data/generated/knowledge/reports/" +
                        "nutrition.unclassified-token-pair-mismatches.json",
            )

        val outputFile =
            File(
                "../data/generated/knowledge/reports/" +
                        "nutrition.unknown-token-pair-mismatches.json",
            )

        require(sourceFile.isFile) {
            "Nutrition token-pair mismatch classification does not exist: " +
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
                        NutritionUnclassifiedTokenPairMismatchClassification::
                        class.java,
                    )
                }

        val analysis =
            NutritionUnknownTokenPairMismatchAnalyzer()
                .analyze(
                    source = source,
                )

        NutritionUnknownTokenPairMismatchAnalysisWriter()
            .write(
                analysis = analysis,
                outputFile = outputFile,
            )

        assertEquals(
            expected =
                analysis.sourcePrimaryUnknownRelationshipCount,
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
                analysis.analyzedTokenPairObservationCount,
            actual =
                analysis.countsByTokenPair.values.sum(),
        )

        assertEquals(
            expected =
                analysis.analyzedTokenPairObservationCount,
            actual =
                analysis.countsByCatalogTokenKind.values.sum(),
        )

        assertEquals(
            expected =
                analysis.analyzedTokenPairObservationCount,
            actual =
                analysis.countsByServerTokenKind.values.sum(),
        )

        assertEquals(
            expected =
                analysis.analyzedTokenPairObservationCount,
            actual =
                analysis.countsByCatalogFoodDomainClass.values.sum(),
        )

        assertEquals(
            expected =
                analysis.analyzedTokenPairObservationCount,
            actual =
                analysis.countsByServerFoodDomainClass.values.sum(),
        )

        assertEquals(
            expected =
                analysis.analyzedTokenPairObservationCount,
            actual =
                analysis.countsByFoodDomainClassPair.values.sum(),
        )

        assertEquals(
            expected =
                analysis.analyzedTokenPairObservationCount,
            actual =
                analysis.countsByPairProfile.values.sum(),
        )

        assertTrue(
            analysis.entries.isNotEmpty(),
            "Expected UNKNOWN token-pair mismatch entries.",
        )

        assertTrue(
            outputFile.isFile,
            "Expected UNKNOWN token-pair mismatch output file.",
        )

        println()
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        )
        println(
            "NUTRITION UNKNOWN TOKEN-PAIR MISMATCHES",
        )
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        )
        println(
            "Source relationships       : " +
                    analysis.sourceRelationshipCount,
        )
        println(
            "Primary UNKNOWN relations  : " +
                    analysis.sourcePrimaryUnknownRelationshipCount,
        )
        println(
            "Analyzed relationships     : " +
                    analysis.analyzedRelationshipCount,
        )
        println(
            "Analyzed observations      : " +
                    analysis.analyzedTokenPairObservationCount,
        )
        println(
            "Single-token relationships : " +
                    analysis.singleTokenPairRelationshipCount,
        )
        println(
            "Multi-token relationships  : " +
                    analysis.multiTokenRelationshipCount,
        )
        println(
            "Catalog token kinds        : " +
                    analysis.countsByCatalogTokenKind,
        )
        println(
            "Server token kinds         : " +
                    analysis.countsByServerTokenKind,
        )
        println(
            "Catalog Food-Domain       : " +
                    analysis.countsByCatalogFoodDomainClass,
        )

        println(
            "Server Food-Domain        : " +
                    analysis.countsByServerFoodDomainClass,
        )

        println(
            "Food-Domain class pairs   : " +
                    analysis.countsByFoodDomainClassPair
                        .entries
                        .sortedWith(
                            compareByDescending<Map.Entry<String, Int>> { entry ->
                                entry.value
                            }.thenBy { entry ->
                                entry.key
                            },
                        )
                        .take(30),
        )
        println(
            "Pair profiles              : " +
                    analysis.countsByPairProfile,
        )
        println(
            "Catalog token frequencies  : " +
                    analysis.countsByCatalogTokenFrequency,
        )
        println(
            "Server token frequencies   : " +
                    analysis.countsByServerTokenFrequency,
        )
        println(
            "Top catalog tokens         : " +
                    analysis.topCatalogTokens.take(20),
        )
        println(
            "Top server tokens          : " +
                    analysis.topServerTokens.take(20),
        )
        println(
            "Top token pairs            : " +
                    analysis.topTokenPairs.take(20),
        )

        analysis.topTokenPairsByProfile
            .forEach { (profile, pairs) ->
                println(
                    "Top $profile: " +
                            pairs.take(10),
                )
            }

        println(
            "Output                     : " +
                    outputFile.path,
        )
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        )
        println()
    }
}