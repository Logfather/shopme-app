package de.shopme.tools.knowledge.pesticides

class DefaultPesticideResolver(

    private val knowledge: PesticideKnowledge

) : PesticideResolver {

    override fun resolve(

        foodReference: String?

    ): Pesticide? {

        val reference =

            foodReference ?: return null

        return knowledge.entries[

            reference.lowercase()

        ]

    }

}