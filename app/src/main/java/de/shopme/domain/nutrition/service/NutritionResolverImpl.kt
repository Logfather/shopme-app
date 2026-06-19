package de.shopme.domain.nutrition.service

import de.shopme.data.sync.logging.RuntimeLog
import de.shopme.domain.nutrition.mapping.NutritionCanonicalMapping
import de.shopme.domain.nutrition.model.NutritionProduct
import de.shopme.domain.nutrition.repository.NutritionRepository
import de.shopme.domain.nutrition.scoring.NutritionCandidateFilter
import de.shopme.domain.nutrition.scoring.NutritionCategoryFilter
import de.shopme.domain.nutrition.scoring.NutritionMatchScorer

class NutritionResolverImpl(
    private val repository: NutritionRepository
) : NutritionResolver {

    override suspend fun resolve(
        productName: String
    ): NutritionProduct? {

        val normalizedQuery =
            NutritionCanonicalMapping
                .normalize(productName)

        val candidates =
            repository.searchProducts(
                normalizedQuery
            )

        if (candidates.isEmpty()) {

            RuntimeLog.runtime(
                "Nutrition Search EMPTY | " +
                        "query=$productName"
            )

            return null
        }

        val candidateFilter =
            NutritionCandidateFilter()

        val categoryFilter =
            NutritionCategoryFilter()

        val filteredCandidates =
            candidates.filter {

                candidateFilter.isAllowed(
                    productName,
                    it.name
                ) &&
                        categoryFilter.isAllowed(
                            productName,
                            it.name
                        )
            }

        if (filteredCandidates.isEmpty()) {

            RuntimeLog.runtime(
                "Nutrition Search EMPTY | " +
                        "query=$productName"
            )

            return null
        }

        val scorer =
            NutritionMatchScorer()

        val (bestMatch, score) =
            filteredCandidates
                .map {
                    it to scorer.score(
                        productName,
                        it.name
                    )
                }
                .maxByOrNull {
                    it.second
                }
                ?: return null

        if (score < 60) {
            return null
        }

        repository.getProductByReference(
            normalizedQuery
        ) ?: run {

            repository.saveReferenceMapping(
                reference = normalizedQuery,
                barcode = bestMatch.barcode
            )
        }

        return repository.getOrFetchProduct(
            bestMatch.barcode
        )
    }
}