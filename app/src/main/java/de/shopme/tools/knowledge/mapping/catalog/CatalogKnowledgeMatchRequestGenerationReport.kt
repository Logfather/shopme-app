package de.shopme.tools.knowledge.mapping.catalog

data class CatalogKnowledgeMatchRequestGenerationReport(
    val artifactName: String,
    val unmatchedCount: Int,
    val requestCount: Int,
    val withoutCandidatesCount: Int,
    val outputFile: String
) {

    init {
        require(artifactName.isNotBlank()) {
            "artifactName must not be blank"
        }

        require(unmatchedCount >= 0) {
            "unmatchedCount must not be negative"
        }

        require(requestCount >= 0) {
            "requestCount must not be negative"
        }

        require(withoutCandidatesCount >= 0) {
            "withoutCandidatesCount must not be negative"
        }

        require(
            requestCount + withoutCandidatesCount ==
                    unmatchedCount
        ) {
            "requestCount + withoutCandidatesCount " +
                    "must equal unmatchedCount"
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
        printLine("CATALOG KNOWLEDGE MATCH REQUESTS")
        printLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        printLine("Artifact           : $artifactName")
        printLine("Unmatched          : $unmatchedCount")
        printLine("Requests generated : $requestCount")
        printLine("Without candidates : $withoutCandidatesCount")
        printLine("Written            : $outputFile")
        printLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}