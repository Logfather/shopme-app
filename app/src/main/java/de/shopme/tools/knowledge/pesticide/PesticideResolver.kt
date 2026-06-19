package de.shopme.tools.knowledge.pesticide

interface PesticideResolver {

    fun resolve(
        foodReference: String?
    ): Pesticide?

}