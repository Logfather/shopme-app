package de.shopme.tools.knowledge.production

import android.content.Context
import de.shopme.tools.knowledge.KnowledgeAssets
import de.shopme.tools.knowledge.loader.JsonKnowledgeLoader

class ProductionGraphLoader(

    context: Context

) : JsonKnowledgeLoader<ProductionKnowledge>(

    context,

    KnowledgeAssets.RUNTIME_ROOT + "production.json",

    ProductionKnowledge::class.java

)