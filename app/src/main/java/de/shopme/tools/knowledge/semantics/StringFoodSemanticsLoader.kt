package de.shopme.tools.knowledge.semantics

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringFoodSemanticsLoader(

    json: String

) : JsonStringKnowledgeLoader<FoodSemanticsKnowledge>(

    json,

    object :
        TypeToken<FoodSemanticsKnowledge>() {}.type

)