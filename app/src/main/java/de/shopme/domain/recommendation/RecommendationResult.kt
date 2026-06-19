package de.shopme.domain.recommendation

data class RecommendationResult(

    val score: Int,

    val reasons: List<RecommendationReason>,

    val suggestions: List<RecommendationSuggestion>

)