package de.shopme.data.nutrition.remote

import de.shopme.data.nutrition.dto.OpenFoodFactsResponseDto
import de.shopme.data.nutrition.dto.OpenFoodFactsSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenFoodFactsApi {

    @GET(
        "api/v2/product/{barcode}.json"
    )
    suspend fun getProduct(
        @Path("barcode")
        barcode: String
    ): OpenFoodFactsResponseDto

    @GET("cgi/search.pl")
    suspend fun searchProducts(

        @Query("search_terms")
        query: String,

        @Query("search_simple")
        searchSimple: Int = 1,

        @Query("action")
        action: String = "process",

        @Query("page_size")
        pageSize: Int = 20,

//        @Query("countries_tags")
//        country: String = "germany",

        @Query("json")
        json: Int = 1
    ): OpenFoodFactsSearchResponseDto
}