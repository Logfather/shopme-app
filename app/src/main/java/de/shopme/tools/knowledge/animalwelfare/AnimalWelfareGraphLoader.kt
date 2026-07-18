package de.shopme.tools.knowledge.animalwelfare

import android.content.Context
import de.shopme.tools.knowledge.KnowledgeAssets
import de.shopme.tools.knowledge.loader.JsonKnowledgeLoader

class AnimalWelfareGraphLoader(

    context: Context

) : JsonKnowledgeLoader<AnimalWelfareKnowledge>(

    context,

    KnowledgeAssets.RUNTIME_ROOT + "animal_welfare.json",

    AnimalWelfareKnowledge::class.java

)