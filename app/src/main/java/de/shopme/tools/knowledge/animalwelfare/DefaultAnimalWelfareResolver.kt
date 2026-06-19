package de.shopme.tools.knowledge.animalwelfare

class DefaultAnimalWelfareResolver(

    private val knowledge: AnimalWelfareKnowledge

) : AnimalWelfareResolver {

    override fun resolve(

        foodReference: String?

    ): AnimalWelfare? {

        val reference =

            foodReference ?: return null

        return knowledge.entries[

            reference.lowercase()

        ]

    }

}