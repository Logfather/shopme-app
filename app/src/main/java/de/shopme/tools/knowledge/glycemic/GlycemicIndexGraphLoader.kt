package de.shopme.tools.knowledge.glycemic

import android.content.Context
import de.shopme.tools.knowledge.KnowledgeAssets
import de.shopme.tools.knowledge.loader.JsonKnowledgeLoader

class GlycemicIndexGraphLoader(

    context: Context

) : JsonKnowledgeLoader<GlycemicIndexKnowledge>(

    context,

    KnowledgeAssets.RUNTIME_ROOT + "glycemic_index.json",

    GlycemicIndexKnowledge::class.java

)