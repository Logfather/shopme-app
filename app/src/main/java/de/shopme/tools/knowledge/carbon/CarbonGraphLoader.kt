package de.shopme.tools.knowledge.carbon

import android.content.Context
import de.shopme.tools.knowledge.KnowledgeAssets
import de.shopme.tools.knowledge.loader.JsonKnowledgeLoader

class CarbonGraphLoader(

    context: Context

) : JsonKnowledgeLoader<CarbonKnowledge>(

    context,

    KnowledgeAssets.ROOT + "carbon_footprint.json",

    CarbonKnowledge::class.java

)