package de.shopme.tools.knowledge.artifacts.json

import com.google.gson.Gson
import de.shopme.tools.knowledge.artifacts.FoodsKnowledgeArtifact
import de.shopme.tools.knowledge.artifacts.FoodsKnowledgeArtifactWriter
import java.io.File

class JsonFoodsKnowledgeArtifactWriter(
    private val gson: Gson = Gson()
) : FoodsKnowledgeArtifactWriter {

    override fun write(
        artifact: FoodsKnowledgeArtifact
    ) {
        error(
            "Output file must be supplied by the caller."
        )
    }

    fun write(
        artifact: FoodsKnowledgeArtifact,
        output: File
    ) {

        output.parentFile?.mkdirs()

        output.writeText(
            gson.toJson(artifact)
        )

    }

}