package de.shopme.tools.knowledge.foods.importer

import de.shopme.tools.knowledge.catalog.CatalogNutritionReferenceParser
import de.shopme.tools.knowledge.foods.FoodKnowledgeSourceEntry
import de.shopme.tools.knowledge.foods.FoodsKnowledge

class FoodsKnowledgeMatcher(
    private val referenceParser: CatalogNutritionReferenceParser =
        CatalogNutritionReferenceParser()
) {

    fun match(
        canonical: FoodsKnowledge,
        incoming: FoodsKnowledge
    ): FoodsKnowledge {

        val canonicalIds =
            canonical.foods
                .map {
                    it.id
                }
                .toSet()

        val canonicalAliasIndex =
            canonical.foods
                .flatMap { food ->

                    food.names.aliases
                        .map { alias ->

                            normalize(alias) to food.id
                        }
                }
                .toMap()

        val matchedFoods =
            incoming.foods
                .mapNotNull { incomingFood ->

                    val canonicalId =
                        findCanonicalId(
                            incomingFood = incomingFood,
                            canonicalIds = canonicalIds,
                            canonicalAliasIndex = canonicalAliasIndex
                        )
                            ?: return@mapNotNull null

                    incomingFood.copy(
                        id = canonicalId,
                        names =
                            incomingFood.names.copy(
                                canonical = canonicalId
                            )
                    )
                }

        return FoodsKnowledge(
            version = incoming.version,
            foods =
                matchedFoods
                    .distinctBy {
                        it.id
                    }
                    .sortedBy {
                        it.id
                    }
        )
    }

    private fun findCanonicalId(
        incomingFood: FoodKnowledgeSourceEntry,
        canonicalIds: Set<String>,
        canonicalAliasIndex: Map<String, String>
    ): String? {

        if (incomingFood.id in canonicalIds) {
            return incomingFood.id
        }

        val normalizedCanonicalName =
            normalize(
                incomingFood.names.canonical
            )

        if (normalizedCanonicalName in canonicalIds) {
            return normalizedCanonicalName
        }

        val aliasMatch =
            incomingFood.names.aliases
                .asSequence()
                .map {
                    normalize(it)
                }
                .mapNotNull {
                    canonicalAliasIndex[it]
                }
                .firstOrNull()

        if (aliasMatch != null) {
            return aliasMatch
        }

        val mappedCanonicalName =
            referenceParser.map(
                incomingFood.names.canonical
            )

        if (mappedCanonicalName in canonicalIds) {
            return mappedCanonicalName
        }

        val mappedAliasMatch =
            incomingFood.names.aliases
                .asSequence()
                .map {
                    referenceParser.map(it)
                }
                .firstOrNull {
                    it in canonicalIds
                }

        if (mappedAliasMatch != null) {
            return mappedAliasMatch
        }

        return null
    }

    private fun normalize(
        value: String
    ): String {

        return value
            .lowercase()
            .replace("ä", "ae")
            .replace("ö", "oe")
            .replace("ü", "ue")
            .replace("ß", "ss")
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()
            .replace(Regex("\\s+"), " ")
    }
}