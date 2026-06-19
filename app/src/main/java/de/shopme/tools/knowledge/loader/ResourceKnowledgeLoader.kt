package de.shopme.tools.knowledge.loader

object ResourceKnowledgeLoader {

    fun load(

        resource: String

    ): String {

        return ResourceKnowledgeLoader::class.java

            .classLoader!!

            .getResourceAsStream(

                resource

            )!!

            .bufferedReader()

            .use {

                it.readText()

            }

    }

}