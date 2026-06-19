package de.shopme.tools.knowledge.fairtrade

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringFairTradeLoader(

    json: String

) : JsonStringKnowledgeLoader<FairTradeKnowledge>(

    json,

    object :

        TypeToken<FairTradeKnowledge>() {}.type

)