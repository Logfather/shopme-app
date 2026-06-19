package de.shopme.tools.knowledge.seasonality

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringSeasonalityLoader(

    json: String

) : JsonStringKnowledgeLoader<SeasonalityKnowledge>(

    json,

    object :
        TypeToken<SeasonalityKnowledge>() {}.type

)