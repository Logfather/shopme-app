package de.shopme.tools.knowledge

import de.shopme.domain.food.FoodKnowledgeEntry
import de.shopme.tools.knowledge.compiler.CompilerContext

interface FoodKnowledgeBuilder {

    fun build(
        context: CompilerContext
    ): FoodKnowledgeEntry

}