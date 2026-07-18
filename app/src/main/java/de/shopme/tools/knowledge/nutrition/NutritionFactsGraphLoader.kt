package de.shopme.tools.knowledge.nutrition

import kotlin.jvm.java
import android.content.Context
import de.shopme.tools.knowledge.KnowledgeAssets
import de.shopme.tools.knowledge.loader.JsonKnowledgeLoader

class NutritionFactsGraphLoader(

    context: Context

) : JsonKnowledgeLoader<NutritionFactsKnowledge>(

    context,

    KnowledgeAssets.RUNTIME_ROOT + "nutrition_facts.json",

    NutritionFactsKnowledge::class.java

)