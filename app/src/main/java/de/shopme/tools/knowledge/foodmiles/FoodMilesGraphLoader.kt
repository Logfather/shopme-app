package de.shopme.tools.knowledge.foodmiles

import android.content.Context
import de.shopme.tools.knowledge.KnowledgeAssets
import de.shopme.tools.knowledge.loader.JsonKnowledgeLoader

class FoodMilesGraphLoader(

    context: Context

) : JsonKnowledgeLoader<FoodMilesKnowledge>(

    context,

    KnowledgeAssets.ROOT +

            "food_miles.json",

    FoodMilesKnowledge::class.java

)