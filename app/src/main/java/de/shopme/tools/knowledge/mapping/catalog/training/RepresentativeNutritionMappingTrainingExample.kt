package de.shopme.tools.knowledge.mapping.catalog.training

import de.shopme.tools.knowledge.mapping.catalog.representative.RepresentativeNutritionMappingDecisionType

data class RepresentativeNutritionMappingTrainingExample(
    val id: String,
    val catalogKey: String,
    val serverArtifact: String,
    val serverKey: String,
    val candidateRank: Int,
    val confidence: Double,
    val decisionType:
    RepresentativeNutritionMappingDecisionType,
    val accepted: Boolean,
    val reasons: List<String>,
    val originalDecisionReason: String?,
    val originalValidationStatus: String,
    val originalValidationReason: String?,
    val provenance:
    RepresentativeNutritionMappingTrainingProvenance
)

data class RepresentativeNutritionMappingTrainingProvenance(
    val sourceType: String,
    val sourceFile: String,
    val sourceVersion: Int,
    val matcher: String,
    val validator: String
)