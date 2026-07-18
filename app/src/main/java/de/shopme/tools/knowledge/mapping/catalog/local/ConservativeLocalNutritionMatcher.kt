package de.shopme.tools.knowledge.mapping.catalog.local

import de.shopme.tools.knowledge.mapping.catalog.training.model.LocalNutritionMatcherCandidate
import de.shopme.tools.knowledge.mapping.catalog.training.model.LocalNutritionMatcherPredictor
import java.io.File
import kotlin.math.abs

class ConservativeLocalNutritionMatcher(
    private val predictor:
    LocalNutritionMatcherPredictor,
    private val autoAcceptThreshold: Double =
        DEFAULT_AUTO_ACCEPT_THRESHOLD
) {

    init {
        require(
            autoAcceptThreshold in 0.0..1.0
        ) {
            "Local matcher auto-accept threshold must be " +
                    "between 0.0 and 1.0."
        }
    }

    fun evaluate(
        catalogKey: String,
        candidates:
        List<LocalNutritionMatcherCandidate>
    ): ConservativeLocalNutritionMatchResult {

        require(catalogKey.isNotBlank()) {
            "Local matcher catalogKey must not be blank."
        }

        require(
            candidates.all {
                it.catalogKey == catalogKey
            }
        ) {
            "All local matcher candidates must belong to " +
                    "the requested catalogKey."
        }

        if (candidates.isEmpty()) {

            return ConservativeLocalNutritionMatchResult(
                catalogKey =
                    catalogKey,
                decisionType =
                    ConservativeLocalNutritionMatchDecisionType
                        .GPT_5_5_FALLBACK,
                selectedServerKey =
                    null,
                probability =
                    null,
                autoAcceptThreshold =
                    autoAcceptThreshold,
                candidateCount =
                    0,
                reason =
                    ConservativeLocalNutritionMatchReason
                        .NO_CANDIDATES
            )
        }

        validateCandidateSet(
            candidates = candidates
        )

        val scoredCandidates =
            candidates
                .map { candidate ->

                    ConservativeLocalNutritionScoredCandidate(
                        serverKey =
                            candidate.serverKey,
                        candidateRank =
                            candidate.candidateRank,
                        probability =
                            predictor.predictProbability(
                                candidate = candidate
                            )
                    )
                }
                .sortedWith(
                    compareByDescending<
                            ConservativeLocalNutritionScoredCandidate
                            > {
                        it.probability
                    }
                        .thenBy {
                            it.candidateRank
                        }
                        .thenBy {
                            it.serverKey
                        }
                )

        val top =
            scoredCandidates.first()

        if (
            top.probability <
            autoAcceptThreshold
        ) {
            return ConservativeLocalNutritionMatchResult(
                catalogKey =
                    catalogKey,
                decisionType =
                    ConservativeLocalNutritionMatchDecisionType
                        .GPT_5_5_FALLBACK,
                selectedServerKey =
                    null,
                probability =
                    top.probability,
                autoAcceptThreshold =
                    autoAcceptThreshold,
                candidateCount =
                    candidates.size,
                reason =
                    ConservativeLocalNutritionMatchReason
                        .TOP_CANDIDATE_BELOW_THRESHOLD
            )
        }

        val second =
            scoredCandidates.getOrNull(1)

        if (
            second != null &&
            probabilitiesEqual(
                first =
                    top.probability,
                second =
                    second.probability
            )
        ) {
            return ConservativeLocalNutritionMatchResult(
                catalogKey =
                    catalogKey,
                decisionType =
                    ConservativeLocalNutritionMatchDecisionType
                        .GPT_5_5_FALLBACK,
                selectedServerKey =
                    null,
                probability =
                    top.probability,
                autoAcceptThreshold =
                    autoAcceptThreshold,
                candidateCount =
                    candidates.size,
                reason =
                    ConservativeLocalNutritionMatchReason
                        .AMBIGUOUS_TOP_PROBABILITY
            )
        }

        return ConservativeLocalNutritionMatchResult(
            catalogKey =
                catalogKey,
            decisionType =
                ConservativeLocalNutritionMatchDecisionType
                    .LOCAL_AUTO_ACCEPT,
            selectedServerKey =
                top.serverKey,
            probability =
                top.probability,
            autoAcceptThreshold =
                autoAcceptThreshold,
            candidateCount =
                candidates.size,
            reason =
                ConservativeLocalNutritionMatchReason
                    .UNIQUE_TOP_CANDIDATE_ABOVE_THRESHOLD
        )
    }

    private fun validateCandidateSet(
        candidates:
        List<LocalNutritionMatcherCandidate>
    ) {
        val expectedCandidateCount =
            candidates.size

        require(
            candidates.all {
                it.candidateCount ==
                        expectedCandidateCount
            }
        ) {
            "Every local matcher candidate must contain the " +
                    "complete candidateCount."
        }

        val ranks =
            candidates
                .map {
                    it.candidateRank
                }
                .sorted()

        require(
            ranks ==
                    (1..expectedCandidateCount).toList()
        ) {
            "Local matcher candidate ranks must be complete, " +
                    "unique and one-based."
        }

        val duplicateServerKeys =
            candidates
                .groupingBy {
                    it.serverKey
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicateServerKeys.isEmpty()) {
            "Duplicate local matcher server keys: " +
                    duplicateServerKeys
                        .sorted()
                        .joinToString()
        }
    }

    private fun probabilitiesEqual(
        first: Double,
        second: Double
    ): Boolean {

        return abs(
            first - second
        ) <= PROBABILITY_EQUALITY_EPSILON
    }

    companion object {

        const val DEFAULT_AUTO_ACCEPT_THRESHOLD =
            0.98

        private const val PROBABILITY_EQUALITY_EPSILON =
            1e-12

        fun fromModelFile(
            modelFile: File,
            autoAcceptThreshold: Double =
                DEFAULT_AUTO_ACCEPT_THRESHOLD
        ): ConservativeLocalNutritionMatcher {

            return ConservativeLocalNutritionMatcher(
                predictor =
                    LocalNutritionMatcherPredictor.fromFile(
                        modelFile = modelFile
                    ),
                autoAcceptThreshold =
                    autoAcceptThreshold
            )
        }
    }
}