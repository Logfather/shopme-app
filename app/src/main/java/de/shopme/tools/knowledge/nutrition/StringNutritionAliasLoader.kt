package de.shopme.tools.knowledge.nutrition

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringNutritionAliasLoader(

    json: String

) : JsonStringKnowledgeLoader<Map<String, String>>(

    json,

    object :
        TypeToken<Map<String, String>>() {}.type

)