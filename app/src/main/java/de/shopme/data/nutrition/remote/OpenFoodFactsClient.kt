package de.shopme.data.nutrition.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OpenFoodFactsClient {

    private const val BASE_URL =
        "https://world.openfoodfacts.org/"

    val api: OpenFoodFactsApi by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(
                OpenFoodFactsApi::class.java
            )
    }
}