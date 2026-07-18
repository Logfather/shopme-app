package de.shopme.tools.knowledge.locality

import android.content.Context
import de.shopme.tools.knowledge.KnowledgeAssets
import de.shopme.tools.knowledge.loader.JsonKnowledgeLoader

class LocalityGraphLoader(

    context: Context

) : JsonKnowledgeLoader<LocalityKnowledge>(

    context,

    KnowledgeAssets.RUNTIME_ROOT +

            "locality.json",

    LocalityKnowledge::class.java

)