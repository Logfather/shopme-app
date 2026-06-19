package de.shopme.tools.knowledge.waterstress

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringWaterStressLoader(

    json: String

) : JsonStringKnowledgeLoader<WaterStressKnowledge>(

    json,

    object :

        TypeToken<WaterStressKnowledge>() {}.type

)