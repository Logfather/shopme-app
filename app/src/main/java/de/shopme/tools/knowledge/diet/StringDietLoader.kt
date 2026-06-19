package de.shopme.tools.knowledge.diet

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringDietLoader(

    json: String

) : JsonStringKnowledgeLoader<DietKnowledge>(

    json,

    object :
        TypeToken<DietKnowledge>() {}.type

)