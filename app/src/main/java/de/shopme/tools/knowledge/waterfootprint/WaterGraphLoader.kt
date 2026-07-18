package de.shopme.tools.knowledge.waterfootprint

import android.content.Context
import de.shopme.tools.knowledge.KnowledgeAssets
import de.shopme.tools.knowledge.loader.JsonKnowledgeLoader

class WaterGraphLoader(

    context: Context

) : JsonKnowledgeLoader<WaterKnowledge>(

    context,

    KnowledgeAssets.RUNTIME_ROOT + "water_footprint.json",

    WaterKnowledge::class.java

)