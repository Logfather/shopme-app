package de.shopme.tools.knowledge.mapping.catalog.training.model

import de.shopme.tools.knowledge.mapping.catalog.training.NutritionDomainMismatchFeatures

data class LocalNutritionMatcherCandidate(
    val catalogKey: String,
    val serverKey: String,
    val candidateRank: Int,
    val candidateCount: Int,
    val diagnosticScore: Double,
    val diagnosticScoreAvailable: Boolean,
    val sharedTokens: List<String>,
    val domainMismatchFeatures:
    NutritionDomainMismatchFeatures? = null,
)