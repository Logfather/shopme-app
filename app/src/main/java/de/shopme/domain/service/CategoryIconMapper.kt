package de.shopme.domain.service

import androidx.compose.runtime.Composable
import de.shopme.ui.illustration.icons.itemicons.*

object CategoryIconMapper {

    @Composable
    fun iconFor(category: String) {

        when (category.lowercase()) {

            //"obst" -> AppleIllustration()

            //"gemüse" -> AppleIllustration()

            //"milchprodukte" -> AppleIllustration()

            //"getränke" -> AppleIllustration()

            //else -> AppleIllustration()
        }
    }
}