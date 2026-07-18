package de.shopme.tools.knowledge.mapping.catalog.local

import de.shopme.tools.knowledge.mapping.catalog.training.model.LocalNutritionMatcherCandidate

class LocalFirstNutritionMatchRouter(
    private val localMatcher:
    ConservativeLocalNutritionMatcher
) {

    fun route(
        catalogKey: String,
        candidates:
        List<LocalNutritionMatcherCandidate>
    ): LocalFirstNutritionMatchRoutingResult {

        val localResult =
            localMatcher.evaluate(
                catalogKey =
                    catalogKey,
                candidates =
                    candidates
            )

        return when (
            localResult.decisionType
        ) {

            ConservativeLocalNutritionMatchDecisionType
                .LOCAL_AUTO_ACCEPT -> {

                LocalFirstNutritionMatchRoutingResult(
                    route =
                        LocalFirstNutritionMatchRoute
                            .LOCAL_DETERMINISTIC_VALIDATION,
                    localResult =
                        localResult
                )
            }

            ConservativeLocalNutritionMatchDecisionType
                .GPT_5_5_FALLBACK -> {

                LocalFirstNutritionMatchRoutingResult(
                    route =
                        LocalFirstNutritionMatchRoute
                            .GPT_5_5,
                    localResult =
                        localResult
                )
            }
        }
    }
}

data class LocalFirstNutritionMatchRoutingResult(
    val route:
    LocalFirstNutritionMatchRoute,
    val localResult:
    ConservativeLocalNutritionMatchResult
)

enum class LocalFirstNutritionMatchRoute {

    /**
     * Aus dem lokalen Ergebnis einen bestehenden
     * RepresentativeNutritionMappingRequest erzeugen und durch
     * DeterministicRepresentativeNutritionMappingValidator schicken.
     */
    LOCAL_DETERMINISTIC_VALIDATION,

    /**
     * Den bestehenden GPT-5.5-Matcher unverändert aufrufen.
     */
    GPT_5_5
}