package de.shopme.tools.knowledge.nutriscore

import kotlin.jvm.java
import android.content.Context
import de.shopme.tools.knowledge.KnowledgeAssets
import de.shopme.tools.knowledge.loader.JsonKnowledgeLoader

class NutriScoreGraphLoader(

    context: Context

) : JsonKnowledgeLoader<NutriScoreFactsKnowledge>(

    context,

    KnowledgeAssets.ROOT + "nutri_score_facts.json",

    NutriScoreFactsKnowledge::class.java

)