package de.shopme.tools.knowledge.pollinator

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringPollinatorLoader(

    json: String

) : JsonStringKnowledgeLoader<PollinatorKnowledge>(

    json,

    object :

        TypeToken<PollinatorKnowledge>() {}.type

)