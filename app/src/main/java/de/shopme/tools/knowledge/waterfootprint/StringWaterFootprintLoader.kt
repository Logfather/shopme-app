package de.shopme.tools.knowledge.waterfootprint

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringWaterFootprintLoader(

    json: String

) : JsonStringKnowledgeLoader<WaterKnowledge>(

    json,

    object :
        TypeToken<WaterKnowledge>() {}.type

)