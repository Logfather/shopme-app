package de.shopme.tools.knowledge.recipegraph

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringRecipeGraphLoader(

    json: String

) : JsonStringKnowledgeLoader<RecipeGraphKnowledge>(

    json,

    object :

        TypeToken<RecipeGraphKnowledge>() {}.type

)