package de.shopme.tools.knowledge.nutrition

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringNutritionFactsLoader(

    json: String

) : JsonStringKnowledgeLoader<NutritionFactsKnowledge>(

    json,

    object :
        TypeToken<NutritionFactsKnowledge>() {}.type

)