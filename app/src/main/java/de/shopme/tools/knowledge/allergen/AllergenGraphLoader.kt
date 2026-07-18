package de.shopme.tools.knowledge.allergen

import android.content.Context
import de.shopme.tools.knowledge.KnowledgeAssets
import de.shopme.tools.knowledge.loader.JsonKnowledgeLoader

class AllergenGraphLoader(

    context: Context

) : JsonKnowledgeLoader<AllergenKnowledge>(

    context,

    KnowledgeAssets.RUNTIME_ROOT + "allergens.json",

    AllergenKnowledge::class.java

)