package de.shopme.tools.knowledge.biodiversity

interface BiodiversityResolver {

    fun resolve(

        foodReference: String?

    ): BiodiversityScore?

}