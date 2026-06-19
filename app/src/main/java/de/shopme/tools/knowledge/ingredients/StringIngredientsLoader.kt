package de.shopme.tools.knowledge.ingredients

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringIngredientsLoader(

    json: String

) : JsonStringKnowledgeLoader<IngredientsKnowledge>(

    json,

    object :
        TypeToken<IngredientsKnowledge>() {}.type

)