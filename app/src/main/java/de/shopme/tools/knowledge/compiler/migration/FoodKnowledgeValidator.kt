package de.shopme.tools.knowledge.compiler.migration

interface FoodKnowledgeValidator {

    fun validate(

        foods: List<FoodDefinitionEntry>

    )

}