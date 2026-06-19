package de.shopme.tools.knowledge.recipe

import com.google.gson.reflect.TypeToken
import de.shopme.tools.knowledge.loader.JsonStringKnowledgeLoader

class StringRecipeLoader(

    json: String

) : JsonStringKnowledgeLoader<RecipeKnowledge>(

    json,

    object :
        TypeToken<RecipeKnowledge>() {}.type

)