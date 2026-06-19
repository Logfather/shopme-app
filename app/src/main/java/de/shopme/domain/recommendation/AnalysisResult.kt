package de.shopme.domain.recommendation

data class AnalysisResult(

    val score: Int,

    val reasons: List<RecommendationReason>,

    val suggestions: List<RecommendationSuggestion>

)