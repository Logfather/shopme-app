package de.shopme.tools.knowledge.loader

import android.content.Context
import de.shopme.tools.knowledge.KnowledgeAssets

class RuntimeKnowledgeLoader<T>(

    context: Context,

    fileName: String,

    clazz: Class<T>

) : JsonKnowledgeLoader<T>(

    context = context,

    assetName = KnowledgeAssets.RUNTIME_ROOT + fileName,

    clazz = clazz

)