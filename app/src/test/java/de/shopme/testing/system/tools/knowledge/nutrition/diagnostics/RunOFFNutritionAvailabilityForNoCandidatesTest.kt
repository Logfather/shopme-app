package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.text.Normalizer
import java.util.Locale
import java.util.zip.GZIPInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Diagnoses whether Open Food Facts contains usable nutrition information for
 * catalog keys that currently produce NO_CANDIDATES in the nutrition matching
 * pipeline.
 *
 * Direct product identity fields and ingredient fields are evaluated
 * separately. A direct product match always takes precedence over an
 * ingredient-only match.
 *
 * This diagnostic deliberately does not modify production artifacts.
 *
 * Input:
 *   ../data/generated/openfoodfacts/
 *       openfoodfacts-products.slim.jsonl.gz
 *
 * Output:
 *   ../data/generated/knowledge/reports/
 *       nutrition.off-availability-for-no-candidates.json
 */
class RunOFFNutritionAvailabilityForNoCandidatesTest {

    private val gson =
        GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create()

    private val targetDefinitions =
        listOf(
            TargetDefinition(
                catalogKey = "chervil",
                aliases =
                    setOf(
                        "chervil",
                        "kerbel",
                    ),
            ),
            TargetDefinition(
                catalogKey = "mace",
                aliases =
                    setOf(
                        "mace",
                        "ground mace",
                        "muskatblüte",
                        "muskatbluete",
                        "macis",
                        "macisblüte",
                        "macisbluete",
                    ),
            ),
            TargetDefinition(
                catalogKey = "salsify",
                aliases =
                    setOf(
                        "salsify",
                        "black salsify",
                        "schwarzwurzel",
                        "schwarzwurzeln",
                    ),
            ),
        )

    @Test
    fun genericNameDoesNotCreateDirectProductIdentityMatch() {
        val product =
            JsonParser
                .parseString(
                    """
                {
                  "code": "0011110228819",
                  "product_name": "King Soopers, Spice Cake",
                  "product_name_en": "King Soopers, Spice Cake",
                  "generic_name": "Water sugar flour cinnamon nutmeg ginger cloves allspice mace maltol cream cheese icing",
                  "ingredients_text": "Water, sugar, flour, cinnamon, nutmeg, ginger, cloves, allspice, mace, maltol."
                }
                """.trimIndent(),
                )
                .asJsonObject

        val directIdentityValues =
            collectDirectIdentityValues(
                product = product,
            )
                .map(::normalizeText)
                .filter(String::isNotBlank)
                .distinct()
                .sorted()

        val ingredientIdentityValues =
            collectIngredientIdentityValues(
                product = product,
            )
                .map(::normalizeText)
                .filter(String::isNotBlank)
                .distinct()
                .sorted()

        assertEquals(
            expected =
                listOf(
                    "king soopers spice cake",
                ),
            actual =
                directIdentityValues,
            message =
                "generic_name must not be treated as direct product identity.",
        )

        assertTrue(
            actual =
                directIdentityValues.none { identityValue ->
                    identityValue
                        .split(' ')
                        .contains("mace")
                },
            message =
                "Mace from generic_name must not occur in direct product identities.",
        )

        assertTrue(
            actual =
                ingredientIdentityValues.any { identityValue ->
                    identityValue
                        .split(' ')
                        .contains("mace")
                },
            message =
                "Mace must remain available as ingredient-only evidence.",
        )
    }

    @Test
    fun diagnoseOFFNutritionAvailabilityForNoCandidatesKeys() {
        val inputFile =
            File(
                "../data/generated/openfoodfacts/" +
                        "openfoodfacts-products.slim.jsonl.gz",
            )

        val outputFile =
            File(
                "../data/generated/knowledge/reports/" +
                        "nutrition.off-availability-for-no-candidates.json",
            )

        assertTrue(
            inputFile.isFile,
            "OFF slim export does not exist: ${inputFile.absolutePath}",
        )

        val accumulators =
            targetDefinitions.associate { definition ->
                definition.catalogKey to
                        TargetAccumulator(
                            definition = definition,
                        )
            }

        val fileResult =
            scanFile(
                file = inputFile,
                accumulators = accumulators,
            )

        val entries =
            targetDefinitions.map { definition ->
                accumulators
                    .getValue(definition.catalogKey)
                    .toReportEntry()
            }

        val report =
            OFFNutritionAvailabilityReport(
                version = 2,
                sourceDirectory = requireNotNull(inputFile.parent) {
                    "Input file has no parent directory."
                },
                sourceFileCount = 1,
                scannedProductCount =
                    fileResult.scannedProductCount,
                targetCount = entries.size,
                entries = entries,
            )

        outputFile.parentFile?.mkdirs()
        outputFile.writeText(
            gson.toJson(report),
            Charsets.UTF_8,
        )

        printReport(
            report = report,
            outputFile = outputFile,
        )

        assertTrue(
            outputFile.isFile,
            "Expected diagnostic report was not written: " +
                    outputFile.absolutePath,
        )
    }

    private fun scanFile(
        file: File,
        accumulators: Map<String, TargetAccumulator>,
    ): FileScanResult {
        var scannedProductCount = 0L
        var sourceLineNumber = 0L

        GZIPInputStream(
            BufferedInputStream(
                FileInputStream(file),
            ),
        ).bufferedReader(Charsets.UTF_8).use { reader ->
            while (true) {
                val line =
                    reader.readLine()
                        ?: break

                sourceLineNumber++

                if (line.isBlank()) {
                    continue
                }

                val element =
                    try {
                        JsonParser.parseString(line)
                    } catch (exception: RuntimeException) {
                        throw IllegalStateException(
                            "Invalid JSONL record in ${file.absolutePath} " +
                                    "at line $sourceLineNumber.",
                            exception,
                        )
                    }

                require(element.isJsonObject) {
                    "Expected JSON object in ${file.absolutePath} " +
                            "at line $sourceLineNumber."
                }

                scannedProductCount++

                inspectProduct(
                    product = element.asJsonObject,
                    sourceFile = file,
                    sourceOrdinal = sourceLineNumber,
                    accumulators = accumulators,
                )
            }
        }

        return FileScanResult(
            scannedProductCount = scannedProductCount,
        )
    }

    private fun inspectProduct(
        product: JsonObject,
        sourceFile: File,
        sourceOrdinal: Long,
        accumulators: Map<String, TargetAccumulator>,
    ) {
        val directIdentityValues =
            collectDirectIdentityValues(
                product = product,
            )
                .map(::normalizeText)
                .filter(String::isNotBlank)
                .distinct()
                .sorted()

        val ingredientIdentityValues =
            collectIngredientIdentityValues(
                product = product,
            )
                .map(::normalizeText)
                .filter(String::isNotBlank)
                .distinct()
                .sorted()

        if (
            directIdentityValues.isEmpty() &&
            ingredientIdentityValues.isEmpty()
        ) {
            return
        }

        val productIdentifier =
            extractProductIdentifier(
                product = product,
                sourceFile = sourceFile,
                sourceOrdinal = sourceOrdinal,
            )

        val nutrientValues =
            collectRecognizedNutrients(
                product = product,
            )

        val availability =
            classifyNutritionAvailability(
                nutrientValues = nutrientValues,
            )

        accumulators.values.forEach { accumulator ->
            val identityMatch =
                determineIdentityMatch(
                    target = accumulator.definition,
                    directIdentityValues = directIdentityValues,
                    ingredientIdentityValues =
                        ingredientIdentityValues,
                )
                    ?: return@forEach

            if (
                !accumulator.seenProductIdentifiers.add(
                    productIdentifier,
                )
            ) {
                return@forEach
            }

            accumulator.record(
                product = product,
                productIdentifier = productIdentifier,
                sourceFile = sourceFile,
                matchOrigin = identityMatch.origin,
                matchedAliases = identityMatch.matchedAliases,
                directIdentityValues =
                    directIdentityValues,
                ingredientIdentityValues =
                    ingredientIdentityValues,
                nutrientValues = nutrientValues,
                availability = availability,
            )
        }
    }

    private fun determineIdentityMatch(
        target: TargetDefinition,
        directIdentityValues: List<String>,
        ingredientIdentityValues: List<String>,
    ): OFFIdentityMatch? {
        val normalizedAliases =
            target.aliases
                .map(::normalizeText)
                .filter(String::isNotBlank)
                .distinct()
                .sorted()

        val directlyMatchedAliases =
            normalizedAliases
                .filter { alias ->
                    directIdentityValues.any { identityValue ->
                        containsNormalizedPhrase(
                            text = identityValue,
                            phrase = alias,
                        )
                    }
                }

        if (directlyMatchedAliases.isNotEmpty()) {
            return OFFIdentityMatch(
                origin =
                    OFFMatchOrigin.DIRECT_PRODUCT_IDENTITY,
                matchedAliases = directlyMatchedAliases,
            )
        }

        val ingredientMatchedAliases =
            normalizedAliases
                .filter { alias ->
                    ingredientIdentityValues.any { identityValue ->
                        containsNormalizedPhrase(
                            text = identityValue,
                            phrase = alias,
                        )
                    }
                }

        if (ingredientMatchedAliases.isEmpty()) {
            return null
        }

        return OFFIdentityMatch(
            origin =
                OFFMatchOrigin.INGREDIENT_ONLY,
            matchedAliases = ingredientMatchedAliases,
        )
    }

    private fun collectDirectIdentityValues(
        product: JsonObject,
    ): List<String> {

        /*
         * Direkte Produktidentitäten dürfen ausschließlich aus Feldern stammen,
         * die tatsächlich den Namen des Produkts repräsentieren.
         *
         * generic_name wird bewusst nicht verwendet:
         *
         * In produktiven OFF-Daten enthält generic_name teilweise lange
         * Beschreibungen oder sogar Zutatenlisten. Dadurch wurde beispielsweise
         * "King Soopers, Spice Cake" als direkter Match für "mace" erkannt,
         * obwohl mace lediglich innerhalb der Zusammensetzung vorkommt.
         *
         * Zutatenfelder werden separat über
         * collectIngredientIdentityValues(...) ausgewertet.
         */
        return buildList {

            addOptionalString(
                product = product,
                fieldName = "product_name",
            )

            addOptionalString(
                product = product,
                fieldName = "product_name_en",
            )

            addOptionalString(
                product = product,
                fieldName = "product_name_de",
            )

            addOptionalString(
                product = product,
                fieldName = "product_name_fr",
            )

            /*
             * Unterstützt zusätzlich die bereits normalisierten internen
             * Diagnose-/Katalogformate.
             */
            addOptionalString(
                product = product,
                fieldName = "itemname",
            )

            addOptionalString(
                product = product,
                fieldName = "productName",
            )

            addOptionalString(
                product = product,
                fieldName = "normalized",
            )

            addOptionalString(
                product = product,
                fieldName = "normalizedName",
            )
        }
    }

    private fun collectIngredientIdentityValues(
        product: JsonObject,
    ): List<String> =
        buildList {
            addOptionalString(
                product = product,
                fieldName = "ingredients_text",
            )
            addOptionalString(
                product = product,
                fieldName = "ingredients_text_en",
            )
            addOptionalString(
                product = product,
                fieldName = "ingredients_text_de",
            )
        }

    private fun MutableList<String>.addOptionalString(
        product: JsonObject,
        fieldName: String,
    ) {
        val value =
            product.get(fieldName)
                ?.takeIf {
                    it.isJsonPrimitive &&
                            it.asJsonPrimitive.isString
                }
                ?.asString
                ?.trim()
                ?.takeIf(String::isNotBlank)
                ?: return

        add(value)
    }

    private fun extractProductIdentifier(
        product: JsonObject,
        sourceFile: File,
        sourceOrdinal: Long,
    ): String {
        val identifierKeys =
            listOf(
                "code",
                "barcode",
                "_id",
                "id",
                "productId",
                "product_id",
            )

        identifierKeys.forEach { key ->
            val value = product.get(key)

            if (
                value != null &&
                value.isJsonPrimitive &&
                value.asJsonPrimitive.isString
            ) {
                val identifier =
                    value.asString.trim()

                if (identifier.isNotEmpty()) {
                    return "$key:$identifier"
                }
            }
        }

        return "${sourceFile.name}:$sourceOrdinal"
    }

    private fun collectRecognizedNutrients(
        product: JsonObject,
    ): Map<NutrientDimension, List<NutrientValue>> {
        val collected =
            NutrientDimension.entries.associateWith {
                mutableListOf<NutrientValue>()
            }

        collectRecognizedNutrientsRecursively(
            element = product,
            path = emptyList(),
            collected = collected,
        )

        return collected
            .mapValues { (_, values) ->
                values
                    .distinctBy { value ->
                        Triple(
                            value.path,
                            value.numericValue,
                            value.rawValue,
                        )
                    }
                    .sortedBy(NutrientValue::path)
            }
            .filterValues(List<NutrientValue>::isNotEmpty)
    }

    private fun collectRecognizedNutrientsRecursively(
        element: JsonElement,
        path: List<String>,
        collected: Map<NutrientDimension, MutableList<NutrientValue>>,
    ) {
        when {
            element.isJsonObject -> {
                element.asJsonObject
                    .entrySet()
                    .forEach { (key, value) ->
                        collectRecognizedNutrientsRecursively(
                            element = value,
                            path = path + key,
                            collected = collected,
                        )
                    }
            }

            element.isJsonArray -> {
                element.asJsonArray
                    .forEachIndexed { index, value ->
                        collectRecognizedNutrientsRecursively(
                            element = value,
                            path = path + index.toString(),
                            collected = collected,
                        )
                    }
            }

            element.isJsonPrimitive &&
                    path.isNotEmpty() -> {
                val numericValue =
                    parseNumericValue(
                        element = element,
                    )
                        ?: return

                if (!numericValue.isFinite()) {
                    return
                }

                val fieldName =
                    path.lastOrNull { pathPart ->
                        pathPart.toIntOrNull() == null
                    }
                        ?: return

                val dimension =
                    NutrientDimension.fromFieldName(
                        rawFieldName = fieldName,
                    )
                        ?: return

                collected
                    .getValue(dimension)
                    .add(
                        NutrientValue(
                            path = path.joinToString("."),
                            rawValue = element.asString,
                            numericValue = numericValue,
                        ),
                    )
            }
        }
    }

    private fun parseNumericValue(
        element: JsonElement,
    ): Double? {
        if (!element.isJsonPrimitive) {
            return null
        }

        val primitive =
            element.asJsonPrimitive

        if (primitive.isNumber) {
            return runCatching {
                primitive.asDouble
            }.getOrNull()
        }

        if (!primitive.isString) {
            return null
        }

        val normalized =
            primitive.asString
                .trim()
                .replace(',', '.')
                .replace(
                    Regex("[^0-9+\\-.]"),
                    "",
                )

        if (normalized.isBlank()) {
            return null
        }

        return normalized.toDoubleOrNull()
    }

    private fun classifyNutritionAvailability(
        nutrientValues: Map<NutrientDimension, List<NutrientValue>>,
    ): NutritionAvailability {
        val presentDimensions =
            nutrientValues
                .filterValues(List<NutrientValue>::isNotEmpty)
                .keys

        val hasAnyNutrition =
            presentDimensions.isNotEmpty()

        val missingCoreDimensions =
            buildList {
                if (NutrientDimension.ENERGY !in presentDimensions) {
                    add(NutrientDimension.ENERGY)
                }

                if (NutrientDimension.FAT !in presentDimensions) {
                    add(NutrientDimension.FAT)
                }

                if (
                    NutrientDimension.CARBOHYDRATES !in
                    presentDimensions
                ) {
                    add(NutrientDimension.CARBOHYDRATES)
                }

                if (NutrientDimension.PROTEIN !in presentDimensions) {
                    add(NutrientDimension.PROTEIN)
                }

                if (
                    NutrientDimension.SALT !in presentDimensions &&
                    NutrientDimension.SODIUM !in presentDimensions
                ) {
                    add(NutrientDimension.SALT)
                }
            }

        val hasCompleteCoreNutrition =
            hasAnyNutrition &&
                    missingCoreDimensions.isEmpty()

        val reason =
            when {
                !hasAnyNutrition ->
                    NutritionAvailabilityReason
                        .NO_NUTRITION_VALUES

                !hasCompleteCoreNutrition ->
                    NutritionAvailabilityReason
                        .INCOMPLETE_CORE_NUTRITION

                else ->
                    NutritionAvailabilityReason
                        .COMPLETE_CORE_NUTRITION
            }

        return NutritionAvailability(
            hasAnyNutrition = hasAnyNutrition,
            hasCompleteCoreNutrition =
                hasCompleteCoreNutrition,
            presentDimensions =
                presentDimensions
                    .map(NutrientDimension::reportName)
                    .sorted(),
            missingCoreDimensions =
                missingCoreDimensions
                    .map(NutrientDimension::reportName)
                    .sorted(),
            reason = reason,
        )
    }

    private fun normalizeText(
        value: String,
    ): String =
        Normalizer
            .normalize(
                value.lowercase(Locale.ROOT),
                Normalizer.Form.NFKD,
            )
            .replace(
                Regex("\\p{M}+"),
                "",
            )
            .replace(
                Regex("[^\\p{L}\\p{N}]+"),
                " ",
            )
            .trim()
            .replace(
                Regex("\\s+"),
                " ",
            )

    private fun containsNormalizedPhrase(
        text: String,
        phrase: String,
    ): Boolean {
        if (
            text.isBlank() ||
            phrase.isBlank()
        ) {
            return false
        }

        if (text == phrase) {
            return true
        }

        val paddedText =
            " $text "
        val paddedPhrase =
            " $phrase "

        return paddedText.contains(paddedPhrase)
    }

    private fun printReport(
        report: OFFNutritionAvailabilityReport,
        outputFile: File,
    ) {
        println()
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        )
        println(
            "OFF NUTRITION AVAILABILITY FOR NO_CANDIDATES",
        )
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        )
        println(
            "Source files scanned : ${report.sourceFileCount}",
        )
        println(
            "Products scanned     : ${report.scannedProductCount}",
        )
        println()

        report.entries.forEach { entry ->
            println(entry.catalogKey)
            println(
                "  aliases                       : " +
                        entry.aliases,
            )
            println(
                "  matching OFF products         : " +
                        entry.matchingOffProductCount,
            )
            println(
                "  direct product matches        : " +
                        entry.directProductMatchCount,
            )
            println(
                "  ingredient-only matches       : " +
                        entry.ingredientOnlyMatchCount,
            )
            println(
                "  direct with any nutrition     : " +
                        entry.directProductsWithAnyNutritionCount,
            )
            println(
                "  direct complete nutrition     : " +
                        entry.directProductsWithCompleteNutritionCount,
            )
            println(
                "  all with any nutrition        : " +
                        entry.productsWithAnyNutritionCount,
            )
            println(
                "  all complete core nutrition   : " +
                        entry.productsWithCompleteNutritionCount,
            )
            println(
                "  all without nutrition         : " +
                        entry.productsWithoutNutritionCount,
            )
            println(
                "  all incomplete core nutrition : " +
                        entry.productsWithIncompleteCoreNutritionCount,
            )
            println(
                "  estimated extractor eligible  : " +
                        entry.estimatedExtractorEligibleCount,
            )
            println(
                "  estimated extractor rejected  : " +
                        entry.estimatedExtractorRejectedCount,
            )
            println(
                "  availability reasons          : " +
                        entry.countsByAvailabilityReason,
            )
            println()
        }

        println("Report written to:")
        println(outputFile.canonicalPath)
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        )
    }

    private data class FileScanResult(
        val scannedProductCount: Long,
    )

    private data class TargetDefinition(
        val catalogKey: String,
        val aliases: Set<String>,
    )

    private class TargetAccumulator(
        val definition: TargetDefinition,
    ) {
        val seenProductIdentifiers =
            mutableSetOf<String>()

        private var directProductMatchCount = 0
        private var ingredientOnlyMatchCount = 0
        private var directProductsWithAnyNutritionCount = 0
        private var directProductsWithCompleteNutritionCount = 0

        private var productsWithAnyNutritionCount = 0
        private var productsWithCompleteNutritionCount = 0
        private var productsWithoutNutritionCount = 0
        private var productsWithIncompleteCoreNutritionCount = 0

        private val countsByAvailabilityReason =
            NutritionAvailabilityReason.entries
                .associateWith {
                    0
                }
                .toMutableMap()

        private val samples =
            mutableListOf<OFFNutritionProductSample>()

        fun record(
            product: JsonObject,
            productIdentifier: String,
            sourceFile: File,
            matchOrigin: OFFMatchOrigin,
            matchedAliases: List<String>,
            directIdentityValues: List<String>,
            ingredientIdentityValues: List<String>,
            nutrientValues:
            Map<NutrientDimension, List<NutrientValue>>,
            availability: NutritionAvailability,
        ) {
            when (matchOrigin) {
                OFFMatchOrigin.DIRECT_PRODUCT_IDENTITY -> {
                    directProductMatchCount++

                    if (availability.hasAnyNutrition) {
                        directProductsWithAnyNutritionCount++
                    }

                    if (availability.hasCompleteCoreNutrition) {
                        directProductsWithCompleteNutritionCount++
                    }
                }

                OFFMatchOrigin.INGREDIENT_ONLY -> {
                    ingredientOnlyMatchCount++
                }
            }

            if (availability.hasAnyNutrition) {
                productsWithAnyNutritionCount++
            } else {
                productsWithoutNutritionCount++
            }

            if (availability.hasCompleteCoreNutrition) {
                productsWithCompleteNutritionCount++
            } else if (availability.hasAnyNutrition) {
                productsWithIncompleteCoreNutritionCount++
            }

            countsByAvailabilityReason[availability.reason] =
                countsByAvailabilityReason
                    .getValue(availability.reason) + 1

            if (samples.size >= MAX_SAMPLE_COUNT) {
                return
            }

            samples +=
                OFFNutritionProductSample(
                    productIdentifier = productIdentifier,
                    sourceFile = sourceFile.name,
                    itemName =
                        firstString(
                            product = product,
                            keys =
                                listOf(
                                    "itemname",
                                    "product_name",
                                    "productName",
                                    "normalized",
                                ),
                        ),
                    normalizedName =
                        firstString(
                            product = product,
                            keys =
                                listOf(
                                    "normalized",
                                    "normalizedName",
                                    "itemname",
                                    "product_name",
                                    "productName",
                                ),
                        ),
                    matchOrigin = matchOrigin.name,
                    matchedAliases = matchedAliases,
                    directIdentityValues =
                        directIdentityValues
                            .take(MAX_IDENTITY_VALUES),
                    ingredientIdentityValues =
                        ingredientIdentityValues
                            .take(MAX_IDENTITY_VALUES),
                    identityValues =
                        (
                                directIdentityValues +
                                        ingredientIdentityValues
                                )
                            .distinct()
                            .sorted()
                            .take(MAX_IDENTITY_VALUES),
                    hasAnyNutrition =
                        availability.hasAnyNutrition,
                    hasCompleteCoreNutrition =
                        availability.hasCompleteCoreNutrition,
                    availabilityReason =
                        availability.reason.name,
                    presentDimensions =
                        availability.presentDimensions,
                    missingCoreDimensions =
                        availability.missingCoreDimensions,
                    nutrientValues =
                        nutrientValues
                            .mapKeys { (dimension, _) ->
                                dimension.reportName
                            }
                            .mapValues { (_, values) ->
                                values.take(
                                    MAX_VALUES_PER_DIMENSION,
                                )
                            },
                )
        }

        fun toReportEntry(): OFFNutritionAvailabilityEntry {
            val matchingOffProductCount =
                seenProductIdentifiers.size

            require(
                directProductMatchCount +
                        ingredientOnlyMatchCount ==
                        matchingOffProductCount,
            ) {
                "Match-origin counts do not cover all products for " +
                        "'${definition.catalogKey}': " +
                        "matching=$matchingOffProductCount, " +
                        "direct=$directProductMatchCount, " +
                        "ingredientOnly=$ingredientOnlyMatchCount."
            }

            val estimatedExtractorEligibleCount =
                directProductsWithCompleteNutritionCount

            val estimatedExtractorRejectedCount =
                directProductMatchCount -
                        estimatedExtractorEligibleCount

            require(estimatedExtractorRejectedCount >= 0) {
                "Estimated rejected count must not be negative for " +
                        "'${definition.catalogKey}'."
            }

            return OFFNutritionAvailabilityEntry(
                catalogKey = definition.catalogKey,
                aliases = definition.aliases.sorted(),
                matchingOffProductCount =
                    matchingOffProductCount,
                directProductMatchCount =
                    directProductMatchCount,
                ingredientOnlyMatchCount =
                    ingredientOnlyMatchCount,
                directProductsWithAnyNutritionCount =
                    directProductsWithAnyNutritionCount,
                directProductsWithCompleteNutritionCount =
                    directProductsWithCompleteNutritionCount,
                productsWithAnyNutritionCount =
                    productsWithAnyNutritionCount,
                productsWithCompleteNutritionCount =
                    productsWithCompleteNutritionCount,
                productsWithoutNutritionCount =
                    productsWithoutNutritionCount,
                productsWithIncompleteCoreNutritionCount =
                    productsWithIncompleteCoreNutritionCount,
                estimatedExtractorEligibleCount =
                    estimatedExtractorEligibleCount,
                estimatedExtractorRejectedCount =
                    estimatedExtractorRejectedCount,
                countsByAvailabilityReason =
                    countsByAvailabilityReason
                        .filterValues { count ->
                            count > 0
                        }
                        .mapKeys { (reason, _) ->
                            reason.name
                        }
                        .toSortedMap(),
                samples = samples,
            )
        }

        private fun firstString(
            product: JsonObject,
            keys: List<String>,
        ): String? {
            keys.forEach { key ->
                val value =
                    product.get(key)

                if (
                    value != null &&
                    value.isJsonPrimitive &&
                    value.asJsonPrimitive.isString
                ) {
                    val text =
                        value.asString.trim()

                    if (text.isNotEmpty()) {
                        return text
                    }
                }
            }

            return null
        }

        companion object {
            private const val MAX_SAMPLE_COUNT = 25
            private const val MAX_IDENTITY_VALUES = 10
            private const val MAX_VALUES_PER_DIMENSION = 5
        }
    }

    private enum class NutritionAvailabilityReason {
        NO_NUTRITION_VALUES,
        INCOMPLETE_CORE_NUTRITION,
        COMPLETE_CORE_NUTRITION,
    }

    private enum class OFFMatchOrigin {
        DIRECT_PRODUCT_IDENTITY,
        INGREDIENT_ONLY,
    }

    private data class OFFIdentityMatch(
        val origin: OFFMatchOrigin,
        val matchedAliases: List<String>,
    )

    private data class NutritionAvailability(
        val hasAnyNutrition: Boolean,
        val hasCompleteCoreNutrition: Boolean,
        val presentDimensions: List<String>,
        val missingCoreDimensions: List<String>,
        val reason: NutritionAvailabilityReason,
    )

    private enum class NutrientDimension(
        val reportName: String,
        private val acceptedFieldNames: Set<String>,
    ) {
        ENERGY(
            reportName = "energy",
            acceptedFieldNames =
                setOf(
                    "energy",
                    "energy100g",
                    "energykj",
                    "energykj100g",
                    "energykcal",
                    "energykcal100g",
                    "calories",
                    "calories100g",
                ),
        ),

        FAT(
            reportName = "fat",
            acceptedFieldNames =
                setOf(
                    "fat",
                    "fat100g",
                    "totalfat",
                    "totalfat100g",
                ),
        ),

        SATURATED_FAT(
            reportName = "saturatedFat",
            acceptedFieldNames =
                setOf(
                    "saturatedfat",
                    "saturatedfat100g",
                    "saturatedfattyacids",
                    "saturatedfattyacids100g",
                    "saturates",
                    "saturates100g",
                ),
        ),

        CARBOHYDRATES(
            reportName = "carbohydrates",
            acceptedFieldNames =
                setOf(
                    "carbohydrate",
                    "carbohydrate100g",
                    "carbohydrates",
                    "carbohydrates100g",
                    "carbs",
                    "carbs100g",
                ),
        ),

        SUGARS(
            reportName = "sugars",
            acceptedFieldNames =
                setOf(
                    "sugar",
                    "sugar100g",
                    "sugars",
                    "sugars100g",
                ),
        ),

        FIBER(
            reportName = "fiber",
            acceptedFieldNames =
                setOf(
                    "fiber",
                    "fiber100g",
                    "fibre",
                    "fibre100g",
                ),
        ),

        PROTEIN(
            reportName = "protein",
            acceptedFieldNames =
                setOf(
                    "protein",
                    "protein100g",
                    "proteins",
                    "proteins100g",
                ),
        ),

        SALT(
            reportName = "salt",
            acceptedFieldNames =
                setOf(
                    "salt",
                    "salt100g",
                ),
        ),

        SODIUM(
            reportName = "sodium",
            acceptedFieldNames =
                setOf(
                    "sodium",
                    "sodium100g",
                ),
        );

        companion object {
            fun fromFieldName(
                rawFieldName: String,
            ): NutrientDimension? {
                val normalizedFieldName =
                    Normalizer
                        .normalize(
                            rawFieldName.lowercase(Locale.ROOT),
                            Normalizer.Form.NFKD,
                        )
                        .replace(
                            Regex("\\p{M}+"),
                            "",
                        )
                        .replace(
                            Regex("[^a-z0-9]"),
                            "",
                        )

                return entries.firstOrNull { dimension ->
                    normalizedFieldName in
                            dimension.acceptedFieldNames
                }
            }
        }
    }

    private data class NutrientValue(
        val path: String,
        val rawValue: String,
        val numericValue: Double,
    )

    private data class OFFNutritionAvailabilityReport(
        val version: Int,
        val sourceDirectory: String,
        val sourceFileCount: Int,
        val scannedProductCount: Long,
        val targetCount: Int,
        val entries: List<OFFNutritionAvailabilityEntry>,
    )

    private data class OFFNutritionAvailabilityEntry(
        val catalogKey: String,
        val aliases: List<String>,
        val matchingOffProductCount: Int,
        val directProductMatchCount: Int,
        val ingredientOnlyMatchCount: Int,
        val directProductsWithAnyNutritionCount: Int,
        val directProductsWithCompleteNutritionCount: Int,
        val productsWithAnyNutritionCount: Int,
        val productsWithCompleteNutritionCount: Int,
        val productsWithoutNutritionCount: Int,
        val productsWithIncompleteCoreNutritionCount: Int,
        /**
         * Estimate based only on direct product matches with complete
         * inspected core dimensions.
         */
        val estimatedExtractorEligibleCount: Int,
        /**
         * Number of direct product matches that are not estimated to be
         * extractor-eligible. Ingredient-only matches are excluded.
         */
        val estimatedExtractorRejectedCount: Int,
        val countsByAvailabilityReason: Map<String, Int>,
        val samples: List<OFFNutritionProductSample>,
    )

    private data class OFFNutritionProductSample(
        val productIdentifier: String,
        val sourceFile: String,
        val itemName: String?,
        val normalizedName: String?,
        val matchOrigin: String,
        val matchedAliases: List<String>,
        val directIdentityValues: List<String>,
        val ingredientIdentityValues: List<String>,
        val identityValues: List<String>,
        val hasAnyNutrition: Boolean,
        val hasCompleteCoreNutrition: Boolean,
        val availabilityReason: String,
        val presentDimensions: List<String>,
        val missingCoreDimensions: List<String>,
        val nutrientValues: Map<String, List<NutrientValue>>,
    )
}