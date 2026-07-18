package de.shopme.tools.knowledge.ai.builder.artifact

import com.google.gson.GsonBuilder
import java.io.File

class GeneratedKnowledgeArtifactWriter(
    private val validator: GeneratedRuntimeKnowledgeArtifactValidator =
        GeneratedRuntimeKnowledgeArtifactValidator()
) {

    private val gson =
        GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create()

    fun write(
        outputDir: File,
        fileName: String,
        artifact: Any
    ): File {
        validator.validate(
            artifactName = fileName,
            artifact = artifact
        )

        outputDir.mkdirs()

        val outputFile =
            File(outputDir, fileName)

        outputFile.writeText(
            gson.toJson(artifact)
        )

        return outputFile
    }
}