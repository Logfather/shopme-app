package de.shopme.tools.knowledge.waterstress

interface WaterStressResolver {

    fun resolve(

        foodReference: String?

    ): WaterStress?

}