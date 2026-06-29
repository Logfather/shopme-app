package de.shopme.tools.knowledge.foods.loader

import android.content.Context
import de.shopme.tools.knowledge.foods.FoodsKnowledge
import de.shopme.tools.knowledge.loader.JsonKnowledgeLoader

class AssetFoodsKnowledgeLoader(

    context: Context

) : JsonKnowledgeLoader<FoodsKnowledge>(

    context = context,

    assetName = "knowledge/runtime/foods.json",

    clazz = FoodsKnowledge::class.java

), FoodsKnowledgeLoader