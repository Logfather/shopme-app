package de.shopme.tools.knowledge.publisher

import java.io.File

class RuntimeKnowledgePublisher(
    private val generatedDirectory: File,
    private val runtimeDirectory: File
) {

    fun publish() {

        runtimeDirectory.mkdirs()

        generatedDirectory
            .listFiles()
            ?.filter { it.extension == "json" }
            ?.forEach {

                it.copyTo(

                    File(

                        runtimeDirectory,

                        it.name

                    ),

                    overwrite = true

                )

            }

    }

}