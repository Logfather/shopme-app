package de.shopme.tools.knowledge.taxonomy

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringFoodTaxonomyLoader(

    json: String

) : JsonStringKnowledgeLoader<FoodTaxonomyKnowledge>(

    json,

    object :
        TypeToken<FoodTaxonomyKnowledge>() {}.type

)