package de.shopme.tools.knowledge.rebuild.nutrition.coverage

data class NutritionNoRequestGapDiagnosticReport(
    val version: Int,
    val noRequestGapCount: Int,
    val reasonCounts: Map<String, Int>,
    val entries: List<NutritionNoRequestGapDiagnosticEntry>
) {

    init {
        require(
            version > 0
        ) {
            "version must be positive."
        }

        require(
            noRequestGapCount >= 0
        ) {
            "noRequestGapCount must not be negative."
        }

        require(
            entries.size == noRequestGapCount
        ) {
            "entries.size must equal noRequestGapCount."
        }

        require(
            reasonCounts.values.sum() == noRequestGapCount
        ) {
            "reasonCounts must cover all NO_REQUEST gaps."
        }

        require(
            entries
                .map {
                    it.catalogKey
                }
                .distinct()
                .size == entries.size
        ) {
            "NO_REQUEST diagnostic entries must have unique catalog keys."
        }
    }
}