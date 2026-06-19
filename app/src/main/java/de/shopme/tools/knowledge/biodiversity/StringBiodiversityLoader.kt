package de.shopme.tools.knowledge.biodiversity

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader


class StringBiodiversityLoader(

    json: String

) : JsonStringKnowledgeLoader<BiodiversityKnowledge>(

    json,

    object :
        TypeToken<BiodiversityKnowledge>() {}.type

)