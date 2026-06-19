package de.shopme.tools.knowledge.nutriscore

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringNutriScoreLoader(

    json: String

) : JsonStringKnowledgeLoader<NutriScoreFactsKnowledge>(

    json,

    object :
        TypeToken<NutriScoreFactsKnowledge>() {}.type

)