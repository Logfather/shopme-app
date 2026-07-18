package de.shopme.tools.knowledge.pesticides

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringPesticideLoader(

    json: String

) : JsonStringKnowledgeLoader<PesticideKnowledge>(

    json,

    object :

        TypeToken<PesticideKnowledge>() {}.type

)