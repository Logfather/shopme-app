package de.shopme.domain.shopbuddy

import de.shopme.presentation.components.ShopBuddyMood

data class ShopBuddyAdvice(

    val mood: ShopBuddyMood,

    val title: String,

    val text: String

)