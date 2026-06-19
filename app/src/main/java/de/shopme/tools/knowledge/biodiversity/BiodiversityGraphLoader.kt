package de.shopme.tools.knowledge.biodiversity

import android.content.Context
import de.shopme.tools.knowledge.KnowledgeAssets
import de.shopme.tools.knowledge.loader.JsonKnowledgeLoader

class BiodiversityGraphLoader(

    context: Context

) : JsonKnowledgeLoader<BiodiversityKnowledge>(

    context,

    KnowledgeAssets.ROOT +

            "biodiversity.json",

    BiodiversityKnowledge::class.java

)