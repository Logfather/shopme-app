package de.shopme.tools.knowledge.loader

object ResourceTextLoader {

    fun load(
        resource: String
    ): String {

        println("REQUESTED: $resource")

        val url =

            ResourceTextLoader::class.java.classLoader

                ?.getResource(resource)

        println("URL: $url")

        check(url != null) {

            "Resource not found: $resource"

        }

        return url

            .readText()

    }
}