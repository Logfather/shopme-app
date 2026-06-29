package de.shopme.tools.knowledge.dimension

class KnowledgeDimensionRegistry(

    val capabilities:

    List<KnowledgeDimensionCapability>

) {

    fun all() =

        capabilities

    fun find(

        id: KnowledgeDimensionId

    ): KnowledgeDimensionCapability? =

        capabilities.firstOrNull {

            it.id == id

        }

}