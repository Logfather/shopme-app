package de.shopme.tools.knowledge.recipe

import android.content.Context
import de.shopme.tools.knowledge.KnowledgeAssets
import de.shopme.tools.knowledge.loader.JsonKnowledgeLoader
import kotlin.jvm.java

class RecipeGraphLoader(

    context: Context

) : JsonKnowledgeLoader<RecipeKnowledge>(

    context,

    KnowledgeAssets.ROOT + "recipe_graph.json",

    RecipeKnowledge::class.java

)