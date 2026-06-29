package de.shopme.tools.knowledge.importer.off.ai

data class OFFAIExtractionBatch(

    /**
     * Herkunft der Daten.
     * Beispiel: Open Food Facts Preview 50k
     */
    val source: String,

    /**
     * Version oder Snapshot der Quelle.
     * Optional.
     */
    val sourceVersion: String? = null,

    /**
     * Produkte, die der AI zur Extraktion übergeben werden.
     */
    val products: List<OFFAIExtractionInput>
)