package de.shopme.tools.knowledge.allergen

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringAllergenLoader(

    json: String

) : JsonStringKnowledgeLoader<AllergenKnowledge>(

    json,

    object :
        TypeToken<AllergenKnowledge>() {}.type

)