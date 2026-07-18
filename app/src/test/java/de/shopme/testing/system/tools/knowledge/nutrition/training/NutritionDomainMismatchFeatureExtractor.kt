package de.shopme.testing.system.tools.knowledge.nutrition.training

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import de.shopme.testing.system.tools.knowledge.nutrition.diagnostics.NutritionFoodDomainMismatchType

class NutritionDomainMismatchFeatureExtractor {

    fun extract(
        mismatchEntry: JsonObject?,
    ): NutritionDomainMismatchFeatures {
        if (mismatchEntry == null) {
            return NutritionDomainMismatchFeatures.empty()
        }

        val primaryMismatchType =
            mismatchEntry
                .getNullableString(
                    propertyName = "primaryMismatchType",
                )

        val observations =
            mismatchEntry
                .getAsJsonArrayOrEmpty(
                    propertyName = "observations",
                )

        val counts =
            NutritionFoodDomainMismatchType.entries
                .associateWith { mismatchType ->
                    observations.countObservations(
                        mismatchType = mismatchType,
                    )
                }

        val identityConflictCount =
            counts.getValue(
                NutritionFoodDomainMismatchType
                    .DIET_OR_SUBSTITUTE_DIFFERENCE,
            ) +
                    counts.getValue(
                        NutritionFoodDomainMismatchType
                            .CROSS_DOMAIN_MISMATCH,
                    ) +
                    counts.getValue(
                        NutritionFoodDomainMismatchType
                            .SAME_DOMAIN_DIFFERENT_ENTITY,
                    )

        val modifierDifferenceCount =
            counts.getValue(
                NutritionFoodDomainMismatchType
                    .FORM_OR_PROCESSING_DIFFERENCE,
            ) +
                    counts.getValue(
                        NutritionFoodDomainMismatchType
                            .REGION_OR_STYLE_DIFFERENCE,
                    )

        val unknownSemanticObservationCount =
            counts.getValue(
                NutritionFoodDomainMismatchType
                    .UNKNOWN_TOKEN_INVOLVED,
            ) +
                    counts.getValue(
                        NutritionFoodDomainMismatchType.UNKNOWN,
                    )

        val knownSemanticObservationCount =
            observations.size() -
                    unknownSemanticObservationCount -
                    counts.getValue(
                        NutritionFoodDomainMismatchType
                            .NON_SEMANTIC_TOKEN_DIFFERENCE,
                    )

        require(knownSemanticObservationCount >= 0) {
            "Known semantic observation count became negative."
        }

        return NutritionDomainMismatchFeatures(
            reportRelationshipPresent = true,
            primaryMismatchType = primaryMismatchType,
            observationCount = observations.size(),
            dietOrSubstituteDifferenceCount =
                counts.getValue(
                    NutritionFoodDomainMismatchType
                        .DIET_OR_SUBSTITUTE_DIFFERENCE,
                ),
            crossDomainMismatchCount =
                counts.getValue(
                    NutritionFoodDomainMismatchType
                        .CROSS_DOMAIN_MISMATCH,
                ),
            sameDomainDifferentEntityCount =
                counts.getValue(
                    NutritionFoodDomainMismatchType
                        .SAME_DOMAIN_DIFFERENT_ENTITY,
                ),
            formOrProcessingDifferenceCount =
                counts.getValue(
                    NutritionFoodDomainMismatchType
                        .FORM_OR_PROCESSING_DIFFERENCE,
                ),
            regionOrStyleDifferenceCount =
                counts.getValue(
                    NutritionFoodDomainMismatchType
                        .REGION_OR_STYLE_DIFFERENCE,
                ),
            compatibleDomainRelationshipCount =
                counts.getValue(
                    NutritionFoodDomainMismatchType
                        .COMPATIBLE_DOMAIN_RELATIONSHIP,
                ),
            unknownTokenInvolvedCount =
                counts.getValue(
                    NutritionFoodDomainMismatchType
                        .UNKNOWN_TOKEN_INVOLVED,
                ),
            nonSemanticTokenDifferenceCount =
                counts.getValue(
                    NutritionFoodDomainMismatchType
                        .NON_SEMANTIC_TOKEN_DIFFERENCE,
                ),
            unknownMismatchCount =
                counts.getValue(
                    NutritionFoodDomainMismatchType.UNKNOWN,
                ),
            identityConflictCount = identityConflictCount,
            modifierDifferenceCount = modifierDifferenceCount,
            knownSemanticObservationCount =
                knownSemanticObservationCount,
            unknownSemanticObservationCount =
                unknownSemanticObservationCount,
        )
    }

    private fun JsonArray.countObservations(
        mismatchType:
        NutritionFoodDomainMismatchType,
    ): Int =
        asSequence()
            .count { observationElement ->
                observationElement
                    .takeIf(JsonElement::isJsonObject)
                    ?.asJsonObject
                    ?.getNullableString(
                        propertyName = "mismatchType",
                    ) ==
                        mismatchType.name
            }

    private fun JsonObject.getNullableString(
        propertyName: String,
    ): String? {
        val element =
            get(propertyName)
                ?: return null

        if (element.isJsonNull) {
            return null
        }

        require(element.isJsonPrimitive) {
            "Expected JSON primitive for '$propertyName'."
        }

        return element.asString
    }

    private fun JsonObject.getAsJsonArrayOrEmpty(
        propertyName: String,
    ): JsonArray {
        val element =
            get(propertyName)
                ?: return JsonArray()

        require(element.isJsonArray) {
            "Expected JSON array for '$propertyName'."
        }

        return element.asJsonArray
    }
}