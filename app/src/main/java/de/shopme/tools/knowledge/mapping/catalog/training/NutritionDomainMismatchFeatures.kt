package de.shopme.tools.knowledge.mapping.catalog.training

/**
 * Persistierte Domain-Mismatch-Merkmale einer Nutrition-
 * Catalog→Server-Kandidatenbeziehung.
 *
 * Die Struktur bildet den JSON-Block "domainMismatchFeatures"
 * exakt ab.
 *
 * Commit 1 transportiert diese Informationen ausschließlich durch
 * die Trainingspipeline. Die Verwendung als Modellfeatures erfolgt
 * erst im nachfolgenden Commit.
 */
data class NutritionDomainMismatchFeatures(
    val version: Int = 1,
    val reportRelationshipPresent: Boolean = false,
    val observationCount: Int = 0,
    val dietOrSubstituteDifferenceCount: Int = 0,
    val crossDomainMismatchCount: Int = 0,
    val sameDomainDifferentEntityCount: Int = 0,
    val formOrProcessingDifferenceCount: Int = 0,
    val regionOrStyleDifferenceCount: Int = 0,
    val compatibleDomainRelationshipCount: Int = 0,
    val unknownTokenInvolvedCount: Int = 0,
    val nonSemanticTokenDifferenceCount: Int = 0,
    val unknownMismatchCount: Int = 0,
    val identityConflictCount: Int = 0,
    val modifierDifferenceCount: Int = 0,
    val knownSemanticObservationCount: Int = 0,
    val unknownSemanticObservationCount: Int = 0,
)