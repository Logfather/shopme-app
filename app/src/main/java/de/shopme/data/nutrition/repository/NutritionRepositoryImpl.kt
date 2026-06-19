package de.shopme.data.nutrition.repository

import de.shopme.data.nutrition.local.NutritionDao
import de.shopme.data.nutrition.local.NutritionReferenceMappingDao
import de.shopme.data.nutrition.local.NutritionReferenceMappingEntity
import de.shopme.data.nutrition.mapper.NutritionMapper
import de.shopme.data.nutrition.remote.OpenFoodFactsDataSource
import de.shopme.data.sync.logging.RuntimeLog
import de.shopme.domain.nutrition.model.NutritionProduct
import de.shopme.domain.nutrition.model.NutritionSearchResult
import de.shopme.domain.nutrition.repository.NutritionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NutritionRepositoryImpl @Inject constructor(
    private val dao: NutritionDao,
    private val mappingDao: NutritionReferenceMappingDao,
    private val remote: OpenFoodFactsDataSource
) : NutritionRepository {

    override suspend fun getProductByBarcode(
        barcode: String
    ): NutritionProduct? {

        return dao.getProduct(barcode)
            ?.let(
                NutritionMapper::toDomain
            )
    }

    override fun observeProduct(
        barcode: String
    ): Flow<NutritionProduct?> {

        return dao.observeProduct(barcode)
            .map { relation ->

                relation?.let(
                    NutritionMapper::toDomain
                )
            }
    }

    override suspend fun refreshProduct(
        barcode: String
    ): NutritionProduct? {

        val remoteProduct =
            remote.getProduct(barcode)
                ?: return getProductByBarcode(barcode)

        saveProduct(remoteProduct)

        return remoteProduct
    }

    override suspend fun saveProduct(
        product: NutritionProduct
    ) {

        dao.upsertProductWithNutrition(
            product =
                NutritionMapper
                    .toProductEntity(product),

            nutrition =
                NutritionMapper
                    .toNutritionEntity(product)
        )
    }

    override suspend fun searchProducts(
        query: String
    ): List<NutritionSearchResult> {

        return try {

            remote.searchProducts(query)

        } catch (e: Exception) {

            RuntimeLog.runtime(
                "Nutrition search fallback " +
                        "query=$query"
            )

            emptyList()
        }
    }


    override suspend fun getProductByReference(
        reference: String
    ): NutritionProduct? {

        val barcode =
            mappingDao.getBarcodeForReference(
                reference
            ) ?: return null

        return getProductByBarcode(
            barcode
        )
    }

    override suspend fun saveReferenceMapping(
        reference: String,
        barcode: String
    ) {

        mappingDao.upsertMapping(
            NutritionReferenceMappingEntity(
                reference = reference,
                barcode = barcode,
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}