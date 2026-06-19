package de.shopme.tools.knowledge.animalwelfare

interface AnimalWelfareResolver {

    fun resolve(

        nutritionReference: String?

    ): AnimalWelfare?

}