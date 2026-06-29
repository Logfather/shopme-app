package de.shopme.tools.knowledge.ki_candidates

import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId

object KnowledgeDimensionCandidateTypeMapper {

    fun map(
        dimension: KnowledgeDimensionId
    ): KnowledgeDimensionCandidateType {

        return when (dimension) {

            KnowledgeDimensionId.NUTRITION ->
                KnowledgeDimensionCandidateType.NUTRITION

            KnowledgeDimensionId.CARBON ->
                KnowledgeDimensionCandidateType.CARBON

            KnowledgeDimensionId.WATER ->
                KnowledgeDimensionCandidateType.WATER

            // ...

            else ->
                throw IllegalArgumentException(
                    "Unsupported knowledge dimension: $dimension"
                )
        }
    }

}