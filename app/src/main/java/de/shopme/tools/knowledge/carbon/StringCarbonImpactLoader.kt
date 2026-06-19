package de.shopme.tools.knowledge.carbon

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringCarbonImpactLoader(

    json: String

) : JsonStringKnowledgeLoader<CarbonImpactKnowledge>(

    json,

    object :
        TypeToken<CarbonImpactKnowledge>() {}.type

)