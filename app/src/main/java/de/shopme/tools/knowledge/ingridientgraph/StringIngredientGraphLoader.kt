package de.shopme.tools.knowledge.ingredientgraph

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringIngredientGraphLoader(

    json: String

) : JsonStringKnowledgeLoader<IngredientGraphKnowledge>(

    json,

    object :

        TypeToken<IngredientGraphKnowledge>() {}.type

)