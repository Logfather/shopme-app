package de.shopme.tools.knowledge.locality

interface LocalityResolver {

    fun resolve(

        foodReference: String?

    ): Locality?

}