package de.shopme.tools.knowledge.importer.off.ai

import de.shopme.tools.knowledge.ai.AIExtractionClient

class OFFAIExtractionClient(
    private val delegate: AIExtractionClient<OFFAIExtractionBatch>
) {

    fun extract(
        batch: OFFAIExtractionBatch
    ) =
        delegate.extract(batch)
}