package de.shopme.tools.knowledge.fairtrade

import android.content.Context
import de.shopme.tools.knowledge.KnowledgeAssets
import de.shopme.tools.knowledge.loader.JsonKnowledgeLoader

class FairTradeGraphLoader(

    context: Context

) : JsonKnowledgeLoader<FairTradeKnowledge>(

    context,

    KnowledgeAssets.ROOT + "fair_trade.json",

    FairTradeKnowledge::class.java

)