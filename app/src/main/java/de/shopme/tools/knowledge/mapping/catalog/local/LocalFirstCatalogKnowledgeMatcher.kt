package de.shopme.tools.knowledge.mapping.catalog.local

import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecision
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisionSource
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisionType
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchRequest
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatcher
import de.shopme.tools.knowledge.mapping.catalog.training.model.LocalNutritionMatcherCandidate

class LocalFirstCatalogKnowledgeMatcher(
    private val localMatcher:
    ConservativeLocalNutritionMatcher,
    private val fallbackMatcher:
    CatalogKnowledgeMatcher
) : CatalogKnowledgeMatcher {

    override fun match(
        request: CatalogKnowledgeMatchRequest
    ): CatalogKnowledgeMatchDecision {

        require(
            request.serverArtifact ==
                    NUTRITION_ARTIFACT
        ) {
            "Local-first matcher only supports nutrition.json: " +
                    request.serverArtifact
        }

        val localCandidates =
            request.candidates
                .mapIndexed { index, candidate ->

                    LocalNutritionMatcherCandidate(
                        catalogKey =
                            request.catalogKey,
                        serverKey =
                            candidate.serverKey,
                        candidateRank =
                            index + 1,
                        candidateCount =
                            request.candidates.size,
                        diagnosticScore =
                            candidate.diagnosticScore,
                        diagnosticScoreAvailable =
                            true,
                        sharedTokens =
                            candidate.sharedTokens,

                    )
                }

        val localResult =
            localMatcher.evaluate(
                catalogKey =
                    request.catalogKey,
                candidates =
                    localCandidates
            )

        return when (
            localResult.decisionType
        ) {

            ConservativeLocalNutritionMatchDecisionType
                .LOCAL_AUTO_ACCEPT -> {

                createLocalDecision(
                    request =
                        request,
                    localResult =
                        localResult
                )
            }

            ConservativeLocalNutritionMatchDecisionType
                .GPT_5_5_FALLBACK -> {

                fallbackMatcher
                    .match(
                        request = request
                    )
                    .copy(
                        decisionSource =
                            CatalogKnowledgeMatchDecisionSource.CHAT_GPT
                    )
            }
        }
    }

    private fun createLocalDecision(
        request: CatalogKnowledgeMatchRequest,
        localResult:
        ConservativeLocalNutritionMatchResult
    ): CatalogKnowledgeMatchDecision {

        val selectedServerKey =
            requireNotNull(
                localResult.selectedServerKey
            ) {
                "LOCAL_AUTO_ACCEPT has no selectedServerKey: " +
                        request.catalogKey
            }

        val probability =
            requireNotNull(
                localResult.probability
            ) {
                "LOCAL_AUTO_ACCEPT has no probability: " +
                        request.catalogKey
            }

        require(
            probability >=
                    localResult.autoAcceptThreshold
        ) {
            "Local auto-accept probability is below threshold: " +
                    "$probability < " +
                    localResult.autoAcceptThreshold
        }

        require(
            request.candidates.any {
                it.serverKey ==
                        selectedServerKey
            }
        ) {
            "Local matcher selected a server key that was not " +
                    "provided as candidate: " +
                    "${request.catalogKey} -> " +
                    selectedServerKey
        }

        return CatalogKnowledgeMatchDecision(
            catalogKey =
                request.catalogKey,
            serverArtifact =
                request.serverArtifact,
            type =
                CatalogKnowledgeMatchDecisionType.MATCH,
            selectedServerKey =
                selectedServerKey,
            confidence =
                probability,
            reason =
                buildString {
                    append("Accepted by local nutrition matcher. ")
                    append("threshold=")
                    append(localResult.autoAcceptThreshold)
                    append(", probability=")
                    append(probability)
                    append(", reason=")
                    append(localResult.reason.name)
                    append(".")
                },
            decisionSource =
                CatalogKnowledgeMatchDecisionSource.LOCAL_MODEL
        )
    }

    private companion object {

        const val NUTRITION_ARTIFACT =
            "nutrition.json"
    }
}