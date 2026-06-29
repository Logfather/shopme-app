package de.shopme.tools.knowledge.foods.loader

import de.shopme.tools.knowledge.foods.FoodsKnowledge

interface FoodsKnowledgeLoader {

    fun load(): FoodsKnowledge

}