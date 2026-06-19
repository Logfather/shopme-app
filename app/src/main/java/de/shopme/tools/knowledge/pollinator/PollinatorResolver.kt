package de.shopme.tools.knowledge.pollinator

interface PollinatorResolver {

    fun resolve(
        foodReference: String?
    ): PollinatorScore?

}