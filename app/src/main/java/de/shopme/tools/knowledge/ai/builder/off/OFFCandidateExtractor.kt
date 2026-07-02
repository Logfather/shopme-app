package de.shopme.tools.knowledge.ai.builder.off

import de.shopme.tools.knowledge.ai.builder.RawKnowledgeInput

class OFFCandidateExtractor {

    fun extract(
        input: RawKnowledgeInput
    ): OFFCandidateData {
        return OFFCandidateData(
            sourceId = input.sourceId,
            name = input.fields["name"] as? String,
            nutrition = input.fields["nutrition"],
            ingredients = input.fields["ingredients"],
            taxonomy = input.fields["taxonomy"],
            allergens = input.fields["allergens"],
            packaging = input.fields["packaging"],
            production = input.fields["production"],
            locality = input.fields["locality"],
            glycemic = input.fields["glycemic"],
            water = input.fields["water"],
            carbon = input.fields["carbon"],
            waterStress = input.fields["waterStress"],
            biodiversity = input.fields["biodiversity"],
            pollinator = input.fields["pollinator"],
            fairtrade = input.fields["fairtrade"],
            animalWelfare = input.fields["animalWelfare"],
            seasonality = input.fields["seasonality"],
            foodMiles = input.fields["foodMiles"],
            recipe = input.fields["recipe"],
            ingredientGraph = input.fields["ingredientGraph"],
            recipeGraph = input.fields["recipeGraph"],
            processing = input.fields["processing"],
        )
    }
}