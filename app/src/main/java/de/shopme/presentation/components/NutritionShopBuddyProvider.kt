package de.shopme.presentation.components

object NutritionShopBuddyProvider {

    fun get(
        nutriScore: String
    ): ShopBuddyState {

        return when (nutriScore.uppercase()) {

            "A" -> ShopBuddyState(
                mood = ShopBuddyMood.Happy,
                text = "Super Wahl!\nSo macht Einkaufen Spaß."
            )

            "B" -> ShopBuddyState(
                mood = ShopBuddyMood.Happy,
                text = "Das passt prima in eine ausgewogene Ernährung."
            )

            "C" -> ShopBuddyState(
                mood = ShopBuddyMood.Thinking,
                text = "Kann man gut kaufen, aber etwas Abwechslung schadet nie."
            )

            "D" -> ShopBuddyState(
                mood = ShopBuddyMood.Warning,
                text = "Ich würde das eher bewusst genießen."
            )

            "E" -> ShopBuddyState(
                mood = ShopBuddyMood.Warning,
                text = "Vielleicht findest du heute eine Alternative mit weniger Zucker oder Fett."
            )

            else -> ShopBuddyState(
                mood = ShopBuddyMood.Idle,
                text = ""
            )
        }
    }
}