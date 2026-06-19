package de.shopme.tools.knowledge.processing

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringProcessingLoader(

    json: String

) : JsonStringKnowledgeLoader<ProcessingKnowledge>(

    json,

    object :
        TypeToken<ProcessingKnowledge>() {}.type

)