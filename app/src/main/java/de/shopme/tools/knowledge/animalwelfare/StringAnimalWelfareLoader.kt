package de.shopme.tools.knowledge.animalwelfare

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringAnimalWelfareLoader(

    json: String

) : JsonStringKnowledgeLoader<AnimalWelfareKnowledge>(

    json,

    object :

        TypeToken<AnimalWelfareKnowledge>() {}.type

)