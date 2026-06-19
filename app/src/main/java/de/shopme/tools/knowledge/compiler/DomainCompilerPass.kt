package de.shopme.tools.knowledge.compiler

import de.shopme.tools.knowledge.compiler.migration.FoodDefinitionEntry


interface DomainCompilerPass {

    fun compile(

        foods: List<FoodDefinitionEntry>

    )

}