package de.shopme.tools.knowledge.packaging

interface PackagingResolver {

    fun resolve(
        foodReference: String?
    ): Packaging?

}