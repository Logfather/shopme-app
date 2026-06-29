package de.shopme.tools.knowledge.foods

import com.google.gson.GsonBuilder
import java.io.File

class FoodsKnowledgeWriter {

    private val gson =
        GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create()

    fun write(
        knowledge: FoodsKnowledge,
        outputFile: File
    ) {
        outputFile.parentFile?.mkdirs()

        outputFile.writeText(
            gson.toJson(knowledge))
    }
}