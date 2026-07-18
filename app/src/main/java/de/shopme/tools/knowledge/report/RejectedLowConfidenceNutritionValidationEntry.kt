package de.shopme.tools.knowledge.report

import de.shopme.tools.knowledge.mapping.catalog.representative.RepresentativeNutritionMappingDecisionType
import de.shopme.tools.knowledge.mapping.catalog.representative.RepresentativeNutritionMappingReason

data class RejectedLowConfidenceNutritionValidationEntry(
    val catalogKey: String,
    val selectedServerKey: String,
    val candidateRank: Int,
    val originalConfidence: Double,
    val originalDecisionReason: String?,
    val originalValidationStatus: String,
    val originalValidationReason: String?,
    val decisionType:
    RepresentativeNutritionMappingDecisionType,
    val reasons:
    List<RepresentativeNutritionMappingReason>,
    val accepted: Boolean
)