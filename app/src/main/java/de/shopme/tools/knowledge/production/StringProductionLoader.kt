package de.shopme.tools.knowledge.production

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringProductionLoader(

    json: String

) : JsonStringKnowledgeLoader<ProductionKnowledge>(

    json,

    object :
        TypeToken<ProductionKnowledge>() {}.type

)