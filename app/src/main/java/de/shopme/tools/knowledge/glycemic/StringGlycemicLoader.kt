package de.shopme.tools.knowledge.glycemic

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringGlycemicLoader(

    json: String

) : JsonStringKnowledgeLoader<GlycemicIndexKnowledge>(

    json,

    object :
        TypeToken<GlycemicIndexKnowledge>() {}.type

)