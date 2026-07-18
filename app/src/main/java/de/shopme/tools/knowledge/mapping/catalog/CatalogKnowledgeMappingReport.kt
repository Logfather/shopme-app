package de.shopme.tools.knowledge.mapping.catalog

data class CatalogKnowledgeMappingReport(
    val artifactName: String,
    val catalogKeyCount: Int,
    val serverKeyCount: Long,
    val exactMappingCount: Int,
    val unmatchedCatalogKeyCount: Int,
    val outputFile: String
) {

    init {
        require(artifactName.isNotBlank()) {
            "artifactName must not be blank"
        }

        require(catalogKeyCount >= 0) {
            "catalogKeyCount must not be negative"
        }

        require(serverKeyCount >= 0L) {
            "serverKeyCount must not be negative"
        }

        require(exactMappingCount >= 0) {
            "exactMappingCount must not be negative"
        }

        require(exactMappingCount <= catalogKeyCount) {
            "exactMappingCount must not exceed catalogKeyCount"
        }

        require(
            unmatchedCatalogKeyCount ==
                    catalogKeyCount - exactMappingCount
        ) {
            "unmatchedCatalogKeyCount must equal " +
                    "catalogKeyCount - exactMappingCount"
        }

        require(outputFile.isNotBlank()) {
            "outputFile must not be blank"
        }
    }


    fun printTo(
        printLine: (String) -> Unit = ::println
    ) {

        printLine("")
        printLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        printLine("CATALOG EXACT KNOWLEDGE MAPPINGS")
        printLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        printLine("Artifact       : $artifactName")
        printLine("Catalog keys   : $catalogKeyCount")
        printLine("Server keys    : $serverKeyCount")
        printLine("Exact mappings : $exactMappingCount")
        printLine("Unmatched      : $unmatchedCatalogKeyCount")
        printLine("Written        : $outputFile")
        printLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}