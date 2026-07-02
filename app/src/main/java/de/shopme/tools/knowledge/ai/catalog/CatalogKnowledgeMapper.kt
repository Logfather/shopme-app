package de.shopme.tools.knowledge.ai.catalog

import de.shopme.domain.catalog.model.KnowledgeReference
import de.shopme.domain.catalog.model.KnowledgeReferences
import de.shopme.tools.knowledge.ki_candidates.CandidateFoodKnowledgePatch
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType

class CatalogKnowledgeMapper {

    fun map(
        patch: CandidateFoodKnowledgePatch
    ): KnowledgeReferences {

        val reference = patch.canonicalId

        return KnowledgeReferences(
            carbonImpact = patch.referenceIfDimensionExists(KnowledgeDimensionCandidateType.CARBON_IMPACT,reference),
            processing = patch.referenceIfDimensionExists(KnowledgeDimensionCandidateType.PROCESSING, reference),
            pesticides = patch.referenceIfDimensionExists(KnowledgeDimensionCandidateType.PESTICIDES, reference),
            nutrition = patch.referenceIfDimensionExists(KnowledgeDimensionCandidateType.NUTRITION, reference),
            ingredients = patch.referenceIfDimensionExists(KnowledgeDimensionCandidateType.INGREDIENTS, reference),
            taxonomy = patch.referenceIfDimensionExists(KnowledgeDimensionCandidateType.TAXONOMY, reference),
            allergens = patch.referenceIfDimensionExists(KnowledgeDimensionCandidateType.ALLERGENS, reference),
            glycemicIndex = patch.referenceIfDimensionExists(KnowledgeDimensionCandidateType.GLYCEMIC, reference),
            carbon = patch.referenceIfDimensionExists(KnowledgeDimensionCandidateType.CARBON, reference),
            water = patch.referenceIfDimensionExists(KnowledgeDimensionCandidateType.WATER, reference),
            waterStress = patch.referenceIfDimensionExists(KnowledgeDimensionCandidateType.WATER_STRESS, reference),
            seasonality = patch.referenceIfDimensionExists(KnowledgeDimensionCandidateType.SEASONALITY, reference),
            packaging = patch.referenceIfDimensionExists(KnowledgeDimensionCandidateType.PACKAGING, reference),
            fairTrade = patch.referenceIfDimensionExists(KnowledgeDimensionCandidateType.FAIRTRADE, reference),
            animalWelfare = patch.referenceIfDimensionExists(KnowledgeDimensionCandidateType.ANIMAL_WELFARE, reference),
            biodiversity = patch.referenceIfDimensionExists(KnowledgeDimensionCandidateType.BIODIVERSITY, reference),
            pollinator = patch.referenceIfDimensionExists(KnowledgeDimensionCandidateType.POLLINATOR, reference),
            locality = patch.referenceIfDimensionExists(KnowledgeDimensionCandidateType.LOCALITY, reference),
            foodMiles = patch.referenceIfDimensionExists(KnowledgeDimensionCandidateType.FOOD_MILES, reference),
            production = patch.referenceIfDimensionExists(KnowledgeDimensionCandidateType.PRODUCTION, reference)
        )
    }

    private fun CandidateFoodKnowledgePatch.referenceIfDimensionExists(
        dimensionType: KnowledgeDimensionCandidateType,
        reference: String
    ): KnowledgeReference? {

        val exists = dimensions.any {
            it.dimension == dimensionType
        }

        if (!exists) {
            return null
        }

        return KnowledgeReference(
            reference = reference,
            source = metadata.source
        )
    }
}