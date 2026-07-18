package de.shopme.tools.knowledge.processing

import android.content.Context
import de.shopme.tools.knowledge.KnowledgeAssets
import de.shopme.tools.knowledge.loader.JsonKnowledgeLoader

class ProcessingGraphLoader(

    context: Context

) : JsonKnowledgeLoader<ProcessingKnowledge>(

    context,

    KnowledgeAssets.RUNTIME_ROOT +

            "processing.json",

    ProcessingKnowledge::class.java

)