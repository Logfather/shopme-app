package de.shopme.tools.knowledge.pesticides

interface PesticideResolver {

    fun resolve(
        foodReference: String?
    ): Pesticide?

}