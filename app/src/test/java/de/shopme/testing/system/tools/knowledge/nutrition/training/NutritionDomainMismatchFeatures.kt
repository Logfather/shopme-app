package de.shopme.testing.system.tools.knowledge.nutrition.training

data class NutritionDomainMismatchFeatures(
    val version: Int = 1,
    val reportRelationshipPresent: Boolean,
    val primaryMismatchType: String?,
    val observationCount: Int,
    val dietOrSubstituteDifferenceCount: Int,
    val crossDomainMismatchCount: Int,
    val sameDomainDifferentEntityCount: Int,
    val formOrProcessingDifferenceCount: Int,
    val regionOrStyleDifferenceCount: Int,
    val compatibleDomainRelationshipCount: Int,
    val unknownTokenInvolvedCount: Int,
    val nonSemanticTokenDifferenceCount: Int,
    val unknownMismatchCount: Int,
    val identityConflictCount: Int,
    val modifierDifferenceCount: Int,
    val knownSemanticObservationCount: Int,
    val unknownSemanticObservationCount: Int,
) {

    companion object {

        fun empty(): NutritionDomainMismatchFeatures =
            NutritionDomainMismatchFeatures(
                reportRelationshipPresent = false,
                primaryMismatchType = null,
                observationCount = 0,
                dietOrSubstituteDifferenceCount = 0,
                crossDomainMismatchCount = 0,
                sameDomainDifferentEntityCount = 0,
                formOrProcessingDifferenceCount = 0,
                regionOrStyleDifferenceCount = 0,
                compatibleDomainRelationshipCount = 0,
                unknownTokenInvolvedCount = 0,
                nonSemanticTokenDifferenceCount = 0,
                unknownMismatchCount = 0,
                identityConflictCount = 0,
                modifierDifferenceCount = 0,
                knownSemanticObservationCount = 0,
                unknownSemanticObservationCount = 0,
            )
    }
}