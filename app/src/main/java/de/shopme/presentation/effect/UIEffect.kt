package de.shopme.presentation.effect

sealed class UIEffect {

    data class ShowSnackbar(
        val message: String
    ) : UIEffect()

    object ShowWelcomeDialog : UIEffect()

    object HideWelcomeDialog : UIEffect()

}