package de.shopme.tools.knowledge.carbon

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringCarbonFootprintLoader(

    json: String

) : JsonStringKnowledgeLoader<CarbonKnowledge>(

    json,

    object :
        TypeToken<CarbonKnowledge>() {}.type

)