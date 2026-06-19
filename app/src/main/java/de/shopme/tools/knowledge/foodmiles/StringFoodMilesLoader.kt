package de.shopme.tools.knowledge.foodmiles

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringFoodMilesLoader(

    json: String

) : JsonStringKnowledgeLoader<FoodMilesKnowledge>(

    json,

    object :

        TypeToken<FoodMilesKnowledge>() {}.type

)