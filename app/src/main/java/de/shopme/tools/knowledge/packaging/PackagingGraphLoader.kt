package de.shopme.tools.knowledge.packaging

import android.content.Context
import de.shopme.tools.knowledge.KnowledgeAssets
import de.shopme.tools.knowledge.loader.JsonKnowledgeLoader

class PackagingGraphLoader(

    context: Context

) : JsonKnowledgeLoader<PackagingKnowledge>(

    context,

    KnowledgeAssets.RUNTIME_ROOT +
            "packaging.json",

    PackagingKnowledge::class.java

)