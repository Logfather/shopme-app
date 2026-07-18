package de.shopme.tools.knowledge.mapping.catalog.training.comparison

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import de.shopme.tools.knowledge.mapping.catalog.training.NutritionMatcherTrainingDataset
import de.shopme.tools.knowledge.mapping.catalog.training.NutritionMatcherTrainingExample
import de.shopme.tools.knowledge.mapping.catalog.training.NutritionMatcherTrainingExampleRole
import de.shopme.tools.knowledge.mapping.catalog.training.NutritionMatcherTrainingLabel
import de.shopme.tools.knowledge.mapping.catalog.training.model.LocalNutritionMatcherModel
import de.shopme.tools.knowledge.mapping.catalog.training.model.LocalNutritionMatcherPredictor
import java.io.File
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale

class LocalNutritionMatcherGptFixtureComparator {

    fun run(
        datasetFile: File,
        modelFile: File,
        outputFile: File,
        output: PrintStream = System.out
    ): CompareLocalNutritionMatcherAgainstGptFixturesResult {

        require(datasetFile.isFile) {
            "Nutrition matcher training dataset does not exist: " +
                    datasetFile.absolutePath
        }

        require(modelFile.isFile) {
            "Local nutrition matcher model does not exist: " +
                    modelFile.absolutePath
        }

        val dataset =
            readDataset(
                file = datasetFile
            )

        val model =
            readModel(
                file = modelFile
            )

        validateContracts(
            dataset = dataset,
            model = model
        )

        val predictor =
            LocalNutritionMatcherPredictor(
                model = model
            )

        val testExamples =
            dataset.examples
                .asSequence()
                .filter {
                    isTestCatalogKey(
                        catalogKey = it.catalogKey,
                        model = model
                    )
                }
                .map { example ->

                    ScoredFixture(
                        example = example,
                        probability =
                            predictor.predictProbability(
                                example = example
                            )
                    )
                }
                .sortedWith(
                    compareBy<ScoredFixture>(
                        { it.example.catalogKey },
                        { it.example.candidateRank },
                        { it.example.serverKey },
                        { it.example.id }
                    )
                )
                .toList()

        require(testExamples.isNotEmpty()) {
            "GPT-5.5 comparison test fixture set is empty."
        }

        require(
            testExamples.any {
                it.example.label ==
                        NutritionMatcherTrainingLabel.POSITIVE
            }
        ) {
            "GPT-5.5 comparison fixtures contain no positives."
        }

        require(
            testExamples.any {
                it.example.label ==
                        NutritionMatcherTrainingLabel.NEGATIVE
            }
        ) {
            "GPT-5.5 comparison fixtures contain no negatives."
        }

        val thresholdComparisons =
            THRESHOLDS.map { threshold ->

                calculateThresholdComparison(
                    fixtures = testExamples,
                    threshold = threshold
                )
            }

        val recommendedThreshold =
            selectRecommendedThreshold(
                comparisons =
                    thresholdComparisons
            )

        val comparison =
            LocalNutritionMatcherGptFixtureComparison(
                datasetFile =
                    datasetFile.name,
                modelFile =
                    modelFile.name,
                testCatalogKeyCount =
                    testExamples
                        .map {
                            it.example.catalogKey
                        }
                        .distinct()
                        .size,
                testExampleCount =
                    testExamples.size,
                positiveFixtureCount =
                    testExamples.count {
                        it.example.label ==
                                NutritionMatcherTrainingLabel.POSITIVE
                    },
                negativeFixtureCount =
                    testExamples.count {
                        it.example.label ==
                                NutritionMatcherTrainingLabel.NEGATIVE
                    },
                thresholds =
                    thresholdComparisons,
                recommendedThreshold =
                    recommendedThreshold,
                topOne =
                    calculateTopOneComparison(
                        fixtures = testExamples
                    )
            )

        validateComparison(
            comparison = comparison
        )

        writeComparison(
            comparison = comparison,
            outputFile = outputFile
        )

        printComparison(
            comparison = comparison,
            outputFile = outputFile,
            output = output
        )

        return CompareLocalNutritionMatcherAgainstGptFixturesResult(
            comparison = comparison,
            outputFile = outputFile.absolutePath
        )
    }

    private fun calculateThresholdComparison(
        fixtures: List<ScoredFixture>,
        threshold: Double
    ): LocalNutritionMatcherThresholdComparison {

        require(threshold in 0.0..1.0)

        var truePositive =
            0

        var falsePositive =
            0

        var trueNegative =
            0

        var falseNegative =
            0

        fixtures.forEach { fixture ->

            val predictedPositive =
                fixture.probability >= threshold

            val actualPositive =
                fixture.example.label ==
                        NutritionMatcherTrainingLabel.POSITIVE

            when {

                predictedPositive &&
                        actualPositive -> {
                    truePositive++
                }

                predictedPositive &&
                        !actualPositive -> {
                    falsePositive++
                }

                !predictedPositive &&
                        actualPositive -> {
                    falseNegative++
                }

                else -> {
                    trueNegative++
                }
            }
        }

        val precision =
            divide(
                numerator = truePositive,
                denominator =
                    truePositive + falsePositive
            )

        val recall =
            divide(
                numerator = truePositive,
                denominator =
                    truePositive + falseNegative
            )

        val specificity =
            divide(
                numerator = trueNegative,
                denominator =
                    trueNegative + falsePositive
            )

        val f1 =
            if (precision + recall == 0.0) {
                0.0
            } else {
                2.0 *
                        precision *
                        recall /
                        (precision + recall)
            }

        return LocalNutritionMatcherThresholdComparison(
            threshold =
                threshold,
            exampleCount =
                fixtures.size,
            positiveCount =
                truePositive + falseNegative,
            negativeCount =
                trueNegative + falsePositive,
            truePositive =
                truePositive,
            falsePositive =
                falsePositive,
            trueNegative =
                trueNegative,
            falseNegative =
                falseNegative,
            precision =
                precision,
            recall =
                recall,
            f1 =
                f1,
            specificity =
                specificity,
            balancedAccuracy =
                (recall + specificity) / 2.0,
            predictedPositiveCount =
                truePositive + falsePositive,
            predictedNegativeCount =
                trueNegative + falseNegative,
            acceptedOriginalMatchRecall =
                calculateRoleRecall(
                    fixtures = fixtures,
                    role =
                        NutritionMatcherTrainingExampleRole
                            .ACCEPTED_ORIGINAL_MATCH,
                    threshold =
                        threshold
                ),
            acceptedRepresentativeRecall =
                calculateRoleRecall(
                    fixtures = fixtures,
                    role =
                        NutritionMatcherTrainingExampleRole
                            .ACCEPTED_SELECTED,
                    threshold =
                        threshold
                ),
            rejectedSelectedFalsePositiveRate =
                calculateRoleFalsePositiveRate(
                    fixtures = fixtures,
                    role =
                        NutritionMatcherTrainingExampleRole
                            .REJECTED_SELECTED,
                    threshold =
                        threshold
                ),
            noMatchFalsePositiveRate =
                calculateRoleFalsePositiveRate(
                    fixtures = fixtures,
                    role =
                        NutritionMatcherTrainingExampleRole
                            .REJECTED_NO_MATCH_CANDIDATE,
                    threshold =
                        threshold
                ),
            alternativeFalsePositiveRate =
                calculateRoleFalsePositiveRate(
                    fixtures = fixtures,
                    role =
                        NutritionMatcherTrainingExampleRole
                            .NON_SELECTED_ALTERNATIVE,
                    threshold =
                        threshold
                )
        )
    }

    private fun calculateRoleRecall(
        fixtures: List<ScoredFixture>,
        role: NutritionMatcherTrainingExampleRole,
        threshold: Double
    ): Double {

        val roleFixtures =
            fixtures.filter {
                it.example.role == role
            }

        if (roleFixtures.isEmpty()) {
            return 0.0
        }

        val positiveFixtures =
            roleFixtures.filter {
                it.example.label ==
                        NutritionMatcherTrainingLabel.POSITIVE
            }

        if (positiveFixtures.isEmpty()) {
            return 0.0
        }

        return positiveFixtures.count {
            it.probability >= threshold
        }
            .toDouble() /
                positiveFixtures.size.toDouble()
    }

    private fun calculateRoleFalsePositiveRate(
        fixtures: List<ScoredFixture>,
        role: NutritionMatcherTrainingExampleRole,
        threshold: Double
    ): Double {

        val negativeFixtures =
            fixtures.filter {
                it.example.role == role &&
                        it.example.label ==
                        NutritionMatcherTrainingLabel.NEGATIVE
            }

        if (negativeFixtures.isEmpty()) {
            return 0.0
        }

        return negativeFixtures.count {
            it.probability >= threshold
        }
            .toDouble() /
                negativeFixtures.size.toDouble()
    }

    private fun selectRecommendedThreshold(
        comparisons:
        List<LocalNutritionMatcherThresholdComparison>
    ): LocalNutritionMatcherRecommendedThreshold? {

        val eligible =
            comparisons
                .filter {
                    it.precision >=
                            MINIMUM_AUTO_ACCEPT_PRECISION
                }
                .filter {
                    it.truePositive > 0
                }

        val selected =
            eligible.maxWithOrNull(
                compareBy<
                        LocalNutritionMatcherThresholdComparison
                        >(
                    { it.recall },
                    { it.f1 },
                    { it.precision },
                    { -it.threshold }
                )
            )
                ?: return null

        return LocalNutritionMatcherRecommendedThreshold(
            threshold =
                selected.threshold,
            minimumPrecision =
                MINIMUM_AUTO_ACCEPT_PRECISION,
            precision =
                selected.precision,
            recall =
                selected.recall,
            f1 =
                selected.f1,
            truePositive =
                selected.truePositive,
            falsePositive =
                selected.falsePositive,
            falseNegative =
                selected.falseNegative
        )
    }

    private fun calculateTopOneComparison(
        fixtures: List<ScoredFixture>
    ): LocalNutritionMatcherTopOneComparison {

        val eligibleGroups =
            fixtures
                .groupBy {
                    it.example.catalogKey
                }
                .filterValues { group ->

                    group.any {
                        it.example.label ==
                                NutritionMatcherTrainingLabel.POSITIVE
                    }
                }

        if (eligibleGroups.isEmpty()) {

            return LocalNutritionMatcherTopOneComparison(
                eligibleCatalogKeyCount = 0,
                correctCatalogKeyCount = 0,
                accuracy = 0.0,
                meanPositiveRank = 0.0,
                positiveAtRankOneCount = 0
            )
        }

        var correctCatalogKeyCount =
            0

        var positiveAtRankOneCount =
            0

        var positiveRankSum =
            0.0

        eligibleGroups
            .toSortedMap()
            .forEach { (_, group) ->

                val ranked =
                    group.sortedWith(
                        compareByDescending<ScoredFixture> {
                            it.probability
                        }
                            .thenBy {
                                it.example.candidateRank
                            }
                            .thenBy {
                                it.example.serverKey
                            }
                            .thenBy {
                                it.example.id
                            }
                    )

                if (
                    ranked.first().example.label ==
                    NutritionMatcherTrainingLabel.POSITIVE
                ) {
                    correctCatalogKeyCount++
                }

                val firstPositiveIndex =
                    ranked.indexOfFirst {
                        it.example.label ==
                                NutritionMatcherTrainingLabel.POSITIVE
                    }

                require(firstPositiveIndex >= 0)

                val positiveRank =
                    firstPositiveIndex + 1

                positiveRankSum +=
                    positiveRank.toDouble()

                if (positiveRank == 1) {
                    positiveAtRankOneCount++
                }
            }

        return LocalNutritionMatcherTopOneComparison(
            eligibleCatalogKeyCount =
                eligibleGroups.size,
            correctCatalogKeyCount =
                correctCatalogKeyCount,
            accuracy =
                correctCatalogKeyCount.toDouble() /
                        eligibleGroups.size.toDouble(),
            meanPositiveRank =
                positiveRankSum /
                        eligibleGroups.size.toDouble(),
            positiveAtRankOneCount =
                positiveAtRankOneCount
        )
    }

    private fun isTestCatalogKey(
        catalogKey: String,
        model: LocalNutritionMatcherModel
    ): Boolean {

        val bucket =
            stableBucket(
                value =
                    catalogKey,
                modulo =
                    model.training.splitModulo
            )

        return bucket in
                model.training.testBuckets
    }

    private fun stableBucket(
        value: String,
        modulo: Int
    ): Int {

        require(modulo > 0)

        val digest =
            MessageDigest
                .getInstance("SHA-256")
                .digest(
                    value.toByteArray(
                        StandardCharsets.UTF_8
                    )
                )

        val unsigned =
            (
                    (digest[0].toInt() and 0xff) shl 24
                    ) or
                    (
                            (digest[1].toInt() and 0xff) shl 16
                            ) or
                    (
                            (digest[2].toInt() and 0xff) shl 8
                            ) or
                    (digest[3].toInt() and 0xff)

        return (unsigned and Int.MAX_VALUE) %
                modulo
    }

    private fun validateContracts(
        dataset: NutritionMatcherTrainingDataset,
        model: LocalNutritionMatcherModel
    ) {
        require(dataset.version == model.training.datasetVersion) {
            "Dataset version differs from trained model metadata."
        }

        require(dataset.datasetType == model.datasetType) {
            "Dataset type differs from trained model."
        }

        require(
            dataset.examples.size ==
                    model.training.exampleCount
        ) {
            "Dataset example count differs from trained model: " +
                    "dataset=${dataset.examples.size}, " +
                    "model=${model.training.exampleCount}"
        }

        require(
            model.training.testBuckets.isNotEmpty()
        ) {
            "Local matcher model contains no test buckets."
        }

        require(
            model.training.testBuckets.all {
                it in 0 until
                        model.training.splitModulo
            }
        ) {
            "Local matcher model contains an invalid test bucket."
        }
    }

    private fun validateComparison(
        comparison:
        LocalNutritionMatcherGptFixtureComparison
    ) {
        require(
            comparison.testExampleCount ==
                    comparison.positiveFixtureCount +
                    comparison.negativeFixtureCount
        )

        require(
            comparison.thresholds.map {
                it.threshold
            } == THRESHOLDS
        )

        require(
            comparison.thresholds.all {
                it.exampleCount ==
                        comparison.testExampleCount
            }
        )

        require(
            comparison.thresholds.all {
                it.truePositive +
                        it.falsePositive +
                        it.trueNegative +
                        it.falseNegative ==
                        comparison.testExampleCount
            }
        )

        require(
            comparison.thresholds.all {
                it.precision in 0.0..1.0 &&
                        it.recall in 0.0..1.0 &&
                        it.f1 in 0.0..1.0 &&
                        it.specificity in 0.0..1.0 &&
                        it.balancedAccuracy in 0.0..1.0
            }
        )

        comparison.recommendedThreshold
            ?.let { recommended ->

                require(
                    recommended.precision >=
                            recommended.minimumPrecision
                )

                require(
                    recommended.threshold in
                            THRESHOLDS
                )
            }
    }

    private fun readDataset(
        file: File
    ): NutritionMatcherTrainingDataset {

        return runCatching {

            Gson().fromJson(
                file.readText(),
                NutritionMatcherTrainingDataset::class.java
            )

        }.getOrElse { throwable ->

            throw IllegalArgumentException(
                "Could not read nutrition matcher training dataset: " +
                        file.absolutePath,
                throwable
            )
        }
    }

    private fun readModel(
        file: File
    ): LocalNutritionMatcherModel {

        return runCatching {

            Gson().fromJson(
                file.readText(),
                LocalNutritionMatcherModel::class.java
            )

        }.getOrElse { throwable ->

            throw IllegalArgumentException(
                "Could not read local nutrition matcher model: " +
                        file.absolutePath,
                throwable
            )
        }
    }

    private fun writeComparison(
        comparison:
        LocalNutritionMatcherGptFixtureComparison,
        outputFile: File
    ) {
        outputFile.parentFile
            ?.let { directory ->

                if (!directory.exists()) {
                    check(directory.mkdirs()) {
                        "Could not create local matcher comparison " +
                                "directory: " +
                                directory.absolutePath
                    }
                }

                require(directory.isDirectory) {
                    "Local matcher comparison parent path is not " +
                            "a directory: " +
                            directory.absolutePath
                }
            }

        val gson =
            GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()

        outputFile.writeText(
            gson.toJson(comparison) + "\n"
        )
    }

    private fun printComparison(
        comparison:
        LocalNutritionMatcherGptFixtureComparison,
        outputFile: File,
        output: PrintStream
    ) {
        output.println()
        output.println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
        output.println(
            "LOCAL NUTRITION MATCHER VS GPT-5.5 FIXTURES"
        )
        output.println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
        output.println(
            "Test catalog keys        : " +
                    comparison.testCatalogKeyCount
        )
        output.println(
            "Test examples            : " +
                    comparison.testExampleCount
        )
        output.println(
            "Positive fixtures        : " +
                    comparison.positiveFixtureCount
        )
        output.println(
            "Negative fixtures        : " +
                    comparison.negativeFixtureCount
        )
        output.println()
        output.println(
            "Threshold  Precision  Recall     F1       FP     FN     NO_MATCH FPR  ALT FPR"
        )

        comparison.thresholds.forEach { threshold ->

            output.println(
                buildString {

                    append(
                        format(
                            threshold.threshold,
                            width = 9
                        )
                    )

                    append(
                        "  "
                    )

                    append(
                        format(
                            threshold.precision,
                            width = 9
                        )
                    )

                    append(
                        "  "
                    )

                    append(
                        format(
                            threshold.recall,
                            width = 7
                        )
                    )

                    append(
                        "  "
                    )

                    append(
                        format(
                            threshold.f1,
                            width = 7
                        )
                    )

                    append(
                        "  "
                    )

                    append(
                        threshold.falsePositive
                            .toString()
                            .padStart(5)
                    )

                    append(
                        "  "
                    )

                    append(
                        threshold.falseNegative
                            .toString()
                            .padStart(5)
                    )

                    append(
                        "  "
                    )

                    append(
                        format(
                            threshold.noMatchFalsePositiveRate,
                            width = 12
                        )
                    )

                    append(
                        "  "
                    )

                    append(
                        format(
                            threshold.alternativeFalsePositiveRate,
                            width = 7
                        )
                    )
                }
            )
        }

        output.println()
        output.println(
            "TOP-1 eligible keys      : " +
                    comparison.topOne.eligibleCatalogKeyCount
        )
        output.println(
            "TOP-1 correct keys       : " +
                    comparison.topOne.correctCatalogKeyCount
        )
        output.println(
            "TOP-1 accuracy           : " +
                    format(
                        comparison.topOne.accuracy
                    )
        )
        output.println(
            "Mean positive rank       : " +
                    format(
                        comparison.topOne.meanPositiveRank
                    )
        )

        output.println()

        val recommended =
            comparison.recommendedThreshold

        if (recommended == null) {

            output.println(
                "Recommended threshold    : NONE"
            )
            output.println(
                "Reason                   : No threshold reached " +
                        "minimum precision " +
                        format(
                            MINIMUM_AUTO_ACCEPT_PRECISION
                        )
            )

        } else {

            output.println(
                "Recommended threshold    : " +
                        format(
                            recommended.threshold
                        )
            )
            output.println(
                "Recommended precision    : " +
                        format(
                            recommended.precision
                        )
            )
            output.println(
                "Recommended recall       : " +
                        format(
                            recommended.recall
                        )
            )
            output.println(
                "Recommended true positive: " +
                        recommended.truePositive
            )
            output.println(
                "Recommended false positive: " +
                        recommended.falsePositive
            )
        }

        output.println()
        output.println(
            "Output                    : " +
                    outputFile.absolutePath
        )
        output.println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
    }

    private fun divide(
        numerator: Int,
        denominator: Int
    ): Double {

        if (denominator == 0) {
            return 0.0
        }

        return numerator.toDouble() /
                denominator.toDouble()
    }

    private fun format(
        value: Double,
        width: Int = 0
    ): String {

        val formatted =
            String.format(
                Locale.ROOT,
                "%.4f",
                value
            )

        return if (width > 0) {
            formatted.padStart(width)
        } else {
            formatted
        }
    }

    private data class ScoredFixture(
        val example:
        NutritionMatcherTrainingExample,
        val probability: Double
    )

    private companion object {

        val THRESHOLDS =
            listOf(
                0.50,
                0.60,
                0.70,
                0.80,
                0.90,
                0.95,
                0.98,
                0.99
            )

        const val MINIMUM_AUTO_ACCEPT_PRECISION =
            0.95
    }
}