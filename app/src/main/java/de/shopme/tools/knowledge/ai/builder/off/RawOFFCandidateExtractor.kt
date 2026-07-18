package de.shopme.tools.knowledge.ai.builder.off

import de.shopme.tools.knowledge.ai.builder.RawKnowledgeInput
import de.shopme.tools.knowledge.ai.builder.double
import de.shopme.tools.knowledge.ai.builder.string
import de.shopme.tools.knowledge.ai.builder.stringList

class RawOFFCandidateExtractor {

    fun RawKnowledgeInput.boolean(
        name: String
    ): Boolean? =
        when (val value = fields[name]) {
            is Boolean -> value
            is String -> value.trim().lowercase().let {
                when (it) {
                    "true", "yes", "1", "ja" -> true
                    "false", "no", "0", "nein" -> false
                    else -> null
                }
            }
            is Number -> value.toInt() != 0
            else -> null
        }

    fun extract(
        input: RawKnowledgeInput
    ): OFFCandidateData {
        return OFFCandidateData(
            sourceId = input.sourceId,
            name = input.string("name"),

            nutrition = input.fields["nutrition"],

            ingredients = input.stringList("ingredients"),
            taxonomy = input.stringList("taxonomy"),
            allergens = input.stringList("allergens"),
            packaging = input.stringList("packaging"),
            production = input.stringList("production"),
            locality = input.stringList("locality"),
            processing = input.stringList("processing"),

            glycemic = input.double("glycemic"),
            water = input.double("water"),
            carbon = input.double("carbon"),
            waterStress = input.double("waterStress"),
            biodiversity = input.double("biodiversity"),
            pollinator = input.double("pollinator"),
            fairtrade = input.boolean("fairtrade"),
            animalWelfare = input.string("animalWelfare"),
            seasonality = input.string("seasonality"),
            foodMiles = input.double("foodMiles"),

            recipe = input.fields["recipe"],
            ingredientGraph = input.fields["ingredientGraph"],
            recipeGraph = input.fields["recipeGraph"]
        )
    }
}