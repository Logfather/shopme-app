package de.shopme.tools.knowledge.seasonality

import android.content.Context
import de.shopme.tools.knowledge.KnowledgeAssets
import de.shopme.tools.knowledge.loader.JsonKnowledgeLoader

class SeasonalityGraphLoader(

    context: Context

) : JsonKnowledgeLoader<SeasonalityKnowledge>(

    context,

    KnowledgeAssets.ROOT + "seasonality.json",

    SeasonalityKnowledge::class.java

)