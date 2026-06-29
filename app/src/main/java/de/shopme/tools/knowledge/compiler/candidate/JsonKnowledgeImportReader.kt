package de.shopme.tools.knowledge.compiler.candidate

import com.google.gson.Gson
import java.io.File

class JsonKnowledgeImportReader(
    private val gson: Gson = Gson()
) : KnowledgeImportReader {

    override fun read(
        file: File
    ): KnowledgeImportBatch {

        return gson.fromJson(
            file.readText(),
            KnowledgeImportBatch::class.java
        )
    }
}