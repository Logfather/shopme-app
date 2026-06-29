package de.shopme.tools.knowledge.publisher

import java.io.File

class KnowledgeArtifactPublisher(

    private val generatedDirectory: File,

    private val knowledgeDirectory: File

) {

    fun publish(
        fileName: String
    ) {

        val source =

            File(
                generatedDirectory,
                fileName
            )

        if (!source.exists()) {
            return
        }

        val target =

            File(
                knowledgeDirectory,
                fileName
            )

        target.parentFile.mkdirs()

        source.copyTo(
            target,
            overwrite = true
        )
    }

}