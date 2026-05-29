package de.shopme.presentation.screens.base

import android.graphics.Color

data class ShoppingItemUi(
    val id: String,
    val name: String,
    val quantity: String?,
    val isChecked: Boolean,
    val categoryColor: Color
)