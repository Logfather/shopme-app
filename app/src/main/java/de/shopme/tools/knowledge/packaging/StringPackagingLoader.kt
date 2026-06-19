package de.shopme.tools.knowledge.packaging

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringPackagingLoader(

    json: String

) : JsonStringKnowledgeLoader<PackagingKnowledge>(

    json,

    object :

        TypeToken<PackagingKnowledge>() {}.type

)