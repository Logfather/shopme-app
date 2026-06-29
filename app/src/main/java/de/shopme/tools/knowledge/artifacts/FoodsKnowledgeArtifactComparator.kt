package de.shopme.tools.knowledge.artifacts

class FoodsKnowledgeArtifactComparator {

    fun compare(
        existingFoods: List<ExistingFoodKnowledgeEntry>,
        generatedArtifact: FoodsKnowledgeArtifact
    ): FoodsKnowledgeArtifactComparison {

        val existingIds =
            existingFoods
                .map { it.normalizedName }
                .toSet()

        val generatedIds =
            generatedArtifact
                .candidates
                .map { it.canonicalId }
                .toSet()

        return FoodsKnowledgeArtifactComparison(
            existingCount = existingIds.size,
            generatedCount = generatedIds.size,
            missingInGenerated = existingIds
                .minus(generatedIds)
                .sorted(),
            missingInExisting = generatedIds
                .minus(existingIds)
                .sorted()
        )

    }

}