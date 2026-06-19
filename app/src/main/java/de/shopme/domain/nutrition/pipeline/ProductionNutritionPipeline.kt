package de.shopme.domain.nutrition.pipeline

import de.shopme.data.nutrition.localfallback.OfflineNutritionDataSource
import de.shopme.domain.nutrition.mapper.NutritionDetailMapper
import de.shopme.presentation.nutrition.model.NutritionDetail
import de.shopme.domain.nutrition.repository.NutritionRepository
import de.shopme.domain.nutrition.service.NutritionResolver

class ProductionNutritionPipeline(

    private val repository: NutritionRepository,

    private val resolver: NutritionResolver

) {

    suspend fun getNutritionDetail(

        reference: String

    ): NutritionDetail? {

        // --------------------------------------------------
        // 1. Room Cache
        // --------------------------------------------------

        repository.getProductByReference(
            reference
        )?.let {

            return NutritionDetailMapper.toDetail(it)

        }

        // --------------------------------------------------
        // 2. Resolver
        // --------------------------------------------------

        val resolvedProduct =

            resolver.resolve(reference)

        // --------------------------------------------------
        // 3. Resolver erfolgreich
        // --------------------------------------------------

        if (resolvedProduct != null) {

            repository.saveReferenceMapping(

                reference = reference,

                barcode = resolvedProduct.barcode

            )

            repository.saveProduct(
                resolvedProduct
            )

            return NutritionDetailMapper.toDetail(
                resolvedProduct
            )
        }

        // --------------------------------------------------
        // 4. Offline Fallback
        // --------------------------------------------------

        val fakeProduct =

            OfflineNutritionDataSource.getProduct(
                reference
            ) ?: return null

        repository.saveReferenceMapping(

            reference = reference,

            barcode = fakeProduct.barcode

        )

        repository.saveProduct(
            fakeProduct
        )

        return NutritionDetailMapper.toDetail(
            fakeProduct
        )
    }
}