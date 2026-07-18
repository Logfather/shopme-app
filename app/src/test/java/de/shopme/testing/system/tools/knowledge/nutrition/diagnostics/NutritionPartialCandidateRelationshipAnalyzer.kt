package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

import java.text.Normalizer
import java.util.Locale

class NutritionPartialCandidateRelationshipAnalyzer {

    fun analyze(
        source: NutritionScoreClusterCandidateAnalysis,
    ): NutritionPartialCandidateRelationshipAnalysis {
        require(source.version > 0) {
            "Source candidate analysis has an invalid version: ${source.version}"
        }

        require(source.entryCount == source.entries.size) {
            "Source entry count does not match actual entries: " +
                    "declared=${source.entryCount}, actual=${source.entries.size}"
        }

        val analyzedEntries =
            source.entries
                .sortedBy { entry ->
                    entry.catalogKey
                }
                .mapNotNull { entry ->
                    analyzeEntry(entry)
                }

        val relationships =
            analyzedEntries.flatMap { entry ->
                entry.relationships
            }

        val expectedPartialCandidateCount =
            source.countsByContainmentType[
                NutritionScoreClusterContainmentType.PARTIAL
            ] ?: 0

        check(relationships.size == expectedPartialCandidateCount) {
            "Partial candidate analysis does not cover all PARTIAL candidates: " +
                    "expected=$expectedPartialCandidateCount, " +
                    "actual=${relationships.size}"
        }

        val countsByPrimaryRelationshipType =
            NutritionPartialCandidateRelationshipType.entries
                .associateWith { relationshipType ->
                    relationships.count { relationship ->
                        relationship.primaryRelationshipType ==
                                relationshipType
                    }
                }

        val countsByDetectedRelationshipType =
            NutritionPartialCandidateRelationshipType.entries
                .associateWith { relationshipType ->
                    relationships.count { relationship ->
                        relationshipType in
                                relationship.detectedRelationshipTypes
                    }
                }

        val countsByCatalogOnlyTokenCount =
            relationships
                .groupingBy { relationship ->
                    relationship.catalogOnlyTokens.size
                }
                .eachCount()
                .toSortedMap()

        val countsByServerOnlyTokenCount =
            relationships
                .groupingBy { relationship ->
                    relationship.serverOnlyTokens.size
                }
                .eachCount()
                .toSortedMap()

        return NutritionPartialCandidateRelationshipAnalysis(
            version = 1,
            sourceEntryCount = source.entryCount,
            sourceCandidateCount = source.candidateCount,
            partialCandidateCount = expectedPartialCandidateCount,
            classifiedCandidateCount = relationships.size,
            countsByPrimaryRelationshipType =
                countsByPrimaryRelationshipType,
            countsByDetectedRelationshipType =
                countsByDetectedRelationshipType,
            countsByCatalogOnlyTokenCount =
                countsByCatalogOnlyTokenCount,
            countsByServerOnlyTokenCount =
                countsByServerOnlyTokenCount,
            entries = analyzedEntries,
        )
    }

    private fun analyzeEntry(
        entry: NutritionScoreClusterCandidateAnalysisEntry,
    ): NutritionPartialCandidateRelationshipEntry? {
        val relationships =
            entry.candidates
                .filter { candidate ->
                    candidate.containmentType ==
                            NutritionScoreClusterContainmentType.PARTIAL
                }
                .sortedBy { candidate ->
                    candidate.rank
                }
                .map { candidate ->
                    analyzeCandidate(
                        catalogKey = entry.catalogKey,
                        candidate = candidate,
                    )
                }

        if (relationships.isEmpty()) {
            return null
        }

        return NutritionPartialCandidateRelationshipEntry(
            catalogKey = entry.catalogKey,
            partialCandidateCount = relationships.size,
            relationships = relationships,
        )
    }

    private fun analyzeCandidate(
        catalogKey: String,
        candidate: NutritionScoreClusterCandidateAnalysisCandidate,
    ): NutritionPartialCandidateRelationship {
        val catalogTokens =
            tokenize(catalogKey)

        val serverTokens =
            tokenize(candidate.serverKey)

        val sharedTokens =
            catalogTokens
                .intersect(serverTokens)
                .sorted()

        val catalogOnlyTokens =
            catalogTokens
                .subtract(serverTokens)
                .sorted()

        val serverOnlyTokens =
            serverTokens
                .subtract(catalogTokens)
                .sorted()

        check(sharedTokens.isNotEmpty()) {
            "PARTIAL candidate has no shared tokens: " +
                    "catalogKey=$catalogKey, " +
                    "serverKey=${candidate.serverKey}"
        }

        val detectedRelationshipTypes =
            detectRelationshipTypes(
                catalogOnlyTokens = catalogOnlyTokens,
                serverOnlyTokens = serverOnlyTokens,
            )

        val primaryRelationshipType =
            determinePrimaryRelationshipType(
                detectedRelationshipTypes =
                    detectedRelationshipTypes,
            )

        return NutritionPartialCandidateRelationship(
            rank = candidate.rank,
            catalogKey = catalogKey,
            serverKey = candidate.serverKey,
            sharedTokens = sharedTokens,
            catalogOnlyTokens = catalogOnlyTokens,
            serverOnlyTokens = serverOnlyTokens,
            catalogCoverage =
                calculateCoverage(
                    sharedTokenCount = sharedTokens.size,
                    totalTokenCount = catalogTokens.size,
                ),
            serverCoverage =
                calculateCoverage(
                    sharedTokenCount = sharedTokens.size,
                    totalTokenCount = serverTokens.size,
                ),
            primaryRelationshipType =
                primaryRelationshipType,
            detectedRelationshipTypes =
                detectedRelationshipTypes
                    .sortedBy { relationshipType ->
                        relationshipType.ordinal
                    },
        )
    }

    private fun detectRelationshipTypes(
        catalogOnlyTokens: List<String>,
        serverOnlyTokens: List<String>,
    ): Set<NutritionPartialCandidateRelationshipType> {
        val unmatchedTokens =
            (catalogOnlyTokens + serverOnlyTokens)
                .toSet()

        val detectedRelationshipTypes =
            linkedSetOf<NutritionPartialCandidateRelationshipType>()

        if (
            containsMorphologicalVariant(
                catalogOnlyTokens = catalogOnlyTokens,
                serverOnlyTokens = serverOnlyTokens,
            )
        ) {
            detectedRelationshipTypes +=
                NutritionPartialCandidateRelationshipType
                    .MORPHOLOGICAL_VARIANT
        }

        if (
            unmatchedTokens.any { token ->
                token in processingStateTokens
            }
        ) {
            detectedRelationshipTypes +=
                NutritionPartialCandidateRelationshipType
                    .PROCESSING_STATE_MISMATCH
        }

        if (
            unmatchedTokens.any { token ->
                token in productFormTokens
            }
        ) {
            detectedRelationshipTypes +=
                NutritionPartialCandidateRelationshipType
                    .PRODUCT_FORM_MISMATCH
        }

        if (
            unmatchedTokens.any { token ->
                token in preparationTokens
            }
        ) {
            detectedRelationshipTypes +=
                NutritionPartialCandidateRelationshipType
                    .PREPARATION_MISMATCH
        }

        if (
            unmatchedTokens.any { token ->
                token in modifierTokens
            }
        ) {
            detectedRelationshipTypes +=
                NutritionPartialCandidateRelationshipType
                    .MODIFIER_MISMATCH
        }

        if (
            unmatchedTokens.any { token ->
                token in specializationTokens
            }
        ) {
            detectedRelationshipTypes +=
                NutritionPartialCandidateRelationshipType
                    .PRODUCT_SPECIALIZATION_MISMATCH
        }

        if (detectedRelationshipTypes.isEmpty()) {
            detectedRelationshipTypes +=
                NutritionPartialCandidateRelationshipType
                    .UNCLASSIFIED_PARTIAL
        }

        return detectedRelationshipTypes
    }

    private fun determinePrimaryRelationshipType(
        detectedRelationshipTypes:
        Set<NutritionPartialCandidateRelationshipType>,
    ): NutritionPartialCandidateRelationshipType {
        val classifiedRelationshipTypes =
            detectedRelationshipTypes.filter { relationshipType ->
                relationshipType !=
                        NutritionPartialCandidateRelationshipType
                            .UNCLASSIFIED_PARTIAL
            }

        return when {
            classifiedRelationshipTypes.size > 1 ->
                NutritionPartialCandidateRelationshipType
                    .MULTIPLE_MISMATCHES

            classifiedRelationshipTypes.size == 1 ->
                classifiedRelationshipTypes.single()

            else ->
                NutritionPartialCandidateRelationshipType
                    .UNCLASSIFIED_PARTIAL
        }
    }

    private fun containsMorphologicalVariant(
        catalogOnlyTokens: List<String>,
        serverOnlyTokens: List<String>,
    ): Boolean =
        catalogOnlyTokens.any { catalogToken ->
            serverOnlyTokens.any { serverToken ->
                singularize(catalogToken) ==
                        singularize(serverToken)
            }
        }

    private fun singularize(
        token: String,
    ): String =
        when {
            token.endsWith("ies") &&
                    token.length > 3 ->
                token.dropLast(3) + "y"

            token.endsWith("ves") &&
                    token.length > 3 ->
                token.dropLast(3) + "f"

            token.endsWith("es") &&
                    token.length > 3 ->
                token.dropLast(2)

            token.endsWith("s") &&
                    token.length > 2 &&
                    !token.endsWith("ss") ->
                token.dropLast(1)

            else ->
                token
        }

    private fun tokenize(
        value: String,
    ): Set<String> =
        normalize(value)
            .split(nonAlphaNumericRegex)
            .asSequence()
            .map { token ->
                token.trim()
            }
            .filter { token ->
                token.isNotBlank()
            }
            .toSortedSet()

    private fun normalize(
        value: String,
    ): String =
        Normalizer
            .normalize(
                value,
                Normalizer.Form.NFKD,
            )
            .replace(
                combiningMarkRegex,
                "",
            )
            .lowercase(Locale.ROOT)
            .trim()

    private fun calculateCoverage(
        sharedTokenCount: Int,
        totalTokenCount: Int,
    ): Double =
        if (totalTokenCount == 0) {
            0.0
        } else {
            sharedTokenCount.toDouble() /
                    totalTokenCount.toDouble()
        }

    private companion object {

        val nonAlphaNumericRegex =
            Regex("[^\\p{L}\\p{N}]+")

        val combiningMarkRegex =
            Regex("\\p{M}+")

        val processingStateTokens =
            setOf(
                "aged",
                "canned",
                "concentrated",
                "dehydrated",
                "dried",
                "dry",
                "fermented",
                "fresh",
                "frozen",
                "instant",
                "pasteurized",
                "powdered",
                "preserved",
                "processed",
                "raw",
                "reconstituted",
                "smoked",
                "sterilized",
            )

        val productFormTokens =
            setOf(
                "bar",
                "beverage",
                "block",
                "chunks",
                "cream",
                "drink",
                "extract",
                "flakes",
                "flour",
                "granules",
                "juice",
                "meal",
                "mince",
                "oil",
                "paste",
                "pieces",
                "powder",
                "puree",
                "sauce",
                "slices",
                "spread",
                "syrup",
                "whole",
            )

        val preparationTokens =
            setOf(
                "baked",
                "boiled",
                "braised",
                "breaded",
                "cooked",
                "fried",
                "grilled",
                "marinated",
                "microwaved",
                "poached",
                "roasted",
                "sauteed",
                "steamed",
                "stewed",
                "toasted",
            )

        val modifierTokens =
            setOf(
                "added",
                "artificial",
                "diet",
                "fat",
                "fatfree",
                "flavored",
                "free",
                "full",
                "high",
                "lean",
                "light",
                "low",
                "natural",
                "nonfat",
                "organic",
                "reduced",
                "salted",
                "skimmed",
                "sweetened",
                "unsalted",
                "unsweetened",
                "vegan",
                "vegetarian",
                "wholegrain",
            )

        val specializationTokens =
            setOf(
                "baby",
                "breakfast",
                "burger",
                "cake",
                "cereal",
                "cheese",
                "dessert",
                "dish",
                "dressing",
                "food",
                "pudding",
                "ready",
                "salad",
                "snack",
                "soup",
                "yogurt",
            )
    }
}