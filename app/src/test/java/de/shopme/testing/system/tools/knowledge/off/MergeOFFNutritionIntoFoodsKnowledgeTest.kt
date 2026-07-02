package de.shopme.testing.system.tools.knowledge.off

import de.shopme.tools.data.KnowledgeDataDirectories
import de.shopme.tools.knowledge.foods.FoodsKnowledgeWriter
import de.shopme.tools.knowledge.foods.importer.FoodsKnowledgeMatcher
import de.shopme.tools.knowledge.foods.importer.FoodsKnowledgeMerger
import de.shopme.tools.knowledge.foods.importer.OFFFoodsKnowledgeImporter
import de.shopme.tools.knowledge.foods.importer.report.FoodsKnowledgeExpansionCandidateAnalyzer
import de.shopme.tools.knowledge.foods.importer.report.FoodsKnowledgeExpansionCandidatePrinter
import de.shopme.tools.knowledge.foods.importer.report.FoodsKnowledgeMatchAnalyzer
import de.shopme.tools.knowledge.foods.importer.report.FoodsKnowledgeMatchPrinter
import de.shopme.tools.knowledge.foods.loader.FileFoodsKnowledgeLoader
import de.shopme.tools.knowledge.foods.report.FoodsKnowledgeCoverageAnalyzer
import de.shopme.tools.knowledge.foods.report.FoodsKnowledgeCoveragePrinter
import java.io.File
import kotlin.test.Test

class MergeOFFNutritionIntoFoodsKnowledgeTest {

    @Test
    fun mergeOFFNutritionIntoFoodsKnowledge() {

        val base =
            FileFoodsKnowledgeLoader(
                File("data/generated/foods.json")
            ).load()

        val importResult =
            OFFFoodsKnowledgeImporter()
                .importWithStatistics(
                    File(
                        KnowledgeDataDirectories.openFoodFactsRaw,
                        "off-products.jsonl.gz"
                    ),
                    limit = 250_000,
                    progressStep = 50_000
                )

        printOFFImportStatistics(
            result = importResult
        )

        val off =
            importResult.knowledge

        val matched =
            FoodsKnowledgeMatcher()
                .match(
                    canonical = base,
                    incoming = off
                )

        val matchReport =
            FoodsKnowledgeMatchAnalyzer()
                .analyze(
                    incoming = off,
                    matched = matched,
                    importResult = importResult
                )

        val expansionCandidates =
            FoodsKnowledgeExpansionCandidateAnalyzer()
                .analyze(
                    canonical = base,
                    matchReport = matchReport,
                    limit = 50
                )

        FoodsKnowledgeExpansionCandidatePrinter()
            .print(
                expansionCandidates
            )

        FoodsKnowledgeMatchPrinter()
            .print(
                matchReport
            )

        val merged =
            FoodsKnowledgeMerger()
                .merge(
                    base = base,
                    incoming = matched
                )

        FoodsKnowledgeWriter()
            .write(
                knowledge = merged,
                outputFile = File("data/generated/foods_merged.json")
            )

        val report =
            FoodsKnowledgeCoverageAnalyzer()
                .analyze(
                    merged
                )

        FoodsKnowledgeCoveragePrinter()
            .print(
                report
            )
    }

    private fun printOFFImportStatistics(
        result: de.shopme.tools.knowledge.importer.OFFFoodsKnowledgeImportResult
    ) {

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OFF IMPORT STATISTICS")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()
        println("Scanned : ${result.scanned}")
        println("Imported: ${result.imported}")
        println("Unique  : ${result.unique}")
        println()
        println("Top imported names:")

        result.nameCounts
            .entries
            .sortedWith(
                compareByDescending<Map.Entry<String, Int>> { it.value }
                    .thenBy { it.key }
            )
            .take(30)
            .forEach { entry ->

                println(
                    "${entry.value} x ${entry.key}"
                )
            }

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()
    }
}