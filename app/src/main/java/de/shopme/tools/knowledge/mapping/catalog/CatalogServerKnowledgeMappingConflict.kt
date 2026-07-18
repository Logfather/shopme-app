package de.shopme.tools.knowledge.mapping.catalog

data class CatalogServerKnowledgeMappingConflict(
    val catalogKey: String,
    val retainedServerKey: String?,
    val conflictingServerKey: String,
    val retainedSourceArtifact: String?,
    val conflictingSourceArtifact: String,
    val reason: String
) {

    init {
        require(catalogKey.isNotBlank()) {
            "catalogKey must not be blank"
        }

        require(
            !retainedServerKey.isNullOrBlank() ||
                    retainedServerKey == null
        ) {
            "retainedServerKey must be null or non-blank"
        }

        require(conflictingServerKey.isNotBlank()) {
            "conflictingServerKey must not be blank"
        }

        require(
            !retainedSourceArtifact.isNullOrBlank() ||
                    retainedSourceArtifact == null
        ) {
            "retainedSourceArtifact must be null or non-blank"
        }

        require(conflictingSourceArtifact.isNotBlank()) {
            "conflictingSourceArtifact must not be blank"
        }

        require(reason.isNotBlank()) {
            "reason must not be blank"
        }
    }


    companion object {

        val ORDER:
                Comparator<CatalogServerKnowledgeMappingConflict> =
            compareBy<CatalogServerKnowledgeMappingConflict> {
                it.catalogKey
            }.thenBy {
                it.retainedServerKey.orEmpty()
            }.thenBy {
                it.conflictingServerKey
            }.thenBy {
                it.conflictingSourceArtifact
            }
    }
}