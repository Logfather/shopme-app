package de.shopme.tools.knowledge.processing

interface ProcessingResolver {

    fun resolve(

        foodReference: String?

    ): ProcessingLevel?

}