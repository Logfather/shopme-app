package de.shopme.tools.knowledge.pollinator

import android.content.Context
import de.shopme.tools.knowledge.KnowledgeAssets
import de.shopme.tools.knowledge.loader.JsonKnowledgeLoader

class PollinatorGraphLoader(

    context: Context

) : JsonKnowledgeLoader<PollinatorKnowledge>(

    context,

    KnowledgeAssets.ROOT + "pollinator.json",

    PollinatorKnowledge::class.java

)