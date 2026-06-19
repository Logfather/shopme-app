package de.shopme.tools.knowledge.pesticide

import android.content.Context
import de.shopme.tools.knowledge.KnowledgeAssets
import de.shopme.tools.knowledge.loader.JsonKnowledgeLoader

class PesticideGraphLoader(

    context: Context

) : JsonKnowledgeLoader<PesticideKnowledge>(

    context,

    KnowledgeAssets.ROOT + "pesticide.json",

    PesticideKnowledge::class.java

)