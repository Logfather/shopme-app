package de.shopme.tools.knowledge.rebuild.nutrition

data class NutritionKnowledgeRebuildPersistenceResult(
    val existingMappingCount: Int,
    val addedMappingCount: Int,
    val unchangedMappingCount: Int,
    val conflictCount: Int,
    val finalMappingCount: Int
) {

    init {
        require(existingMappingCount >= 0)
        require(addedMappingCount >= 0)
        require(unchangedMappingCount >= 0)
        require(conflictCount >= 0)
        require(finalMappingCount >= 0)

        require(
            finalMappingCount ==
                    existingMappingCount +
                    addedMappingCount
        ) {
            "Final mapping count must equal existing plus added."
        }
    }
}