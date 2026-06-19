package de.shopme.domain.shopbuddy

interface ShopBuddyService {

    suspend fun getAdvice(
        request: ShopBuddyRequest
    ): ShopBuddyAdvice

}