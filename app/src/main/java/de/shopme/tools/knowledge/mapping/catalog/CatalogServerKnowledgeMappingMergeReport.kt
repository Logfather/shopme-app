package de.shopme.tools.knowledge.mapping.catalog

data class CatalogServerKnowledgeMappingMergeReport(
    val existingMappingCount: Int,
    val incomingMappingCount: Int,
    val addedMappingCount: Int,
    val unchangedMappingCount: Int,
    val conflictCount: Int,
    val totalMappingCount: Int,
    val conflicts: List<CatalogServerKnowledgeMappingConflict>
) {

    init {
        require(existingMappingCount >= 0) {
            "existingMappingCount must not be negative"
        }

        require(incomingMappingCount >= 0) {
            "incomingMappingCount must not be negative"
        }

        require(addedMappingCount >= 0) {
            "addedMappingCount must not be negative"
        }

        require(unchangedMappingCount >= 0) {
            "unchangedMappingCount must not be negative"
        }

        require(conflictCount >= 0) {
            "conflictCount must not be negative"
        }

        require(totalMappingCount >= 0) {
            "totalMappingCount must not be negative"
        }

        require(conflictCount == conflicts.size) {
            "conflictCount must equal conflicts.size"
        }

        require(
            conflicts ==
                    conflicts.sortedWith(
                        CatalogServerKnowledgeMappingConflict.ORDER
                    )
        ) {
            "conflicts must be deterministically ordered"
        }
    }
}