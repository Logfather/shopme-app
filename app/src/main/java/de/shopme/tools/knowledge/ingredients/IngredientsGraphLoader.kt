package de.shopme.tools.knowledge.ingredients

import android.content.Context
import de.shopme.tools.knowledge.KnowledgeAssets
import de.shopme.tools.knowledge.loader.JsonKnowledgeLoader

class IngredientsGraphLoader(

    context: Context

) : JsonKnowledgeLoader<IngredientsKnowledge>(

    context,

    KnowledgeAssets.ROOT + "ingredients.json",

    IngredientsKnowledge::class.java

)