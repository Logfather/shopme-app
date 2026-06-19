package de.shopme.tools.knowledge.locality

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringLocalityLoader(

    json: String

) : JsonStringKnowledgeLoader<LocalityKnowledge>(

    json,

    object :
        TypeToken<LocalityKnowledge>() {}.type

)