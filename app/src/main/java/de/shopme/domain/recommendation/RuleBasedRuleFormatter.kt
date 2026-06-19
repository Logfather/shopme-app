package de.shopme.domain.recommendation

import de.shopme.domain.shopbuddy.ShopBuddyAdvice
import de.shopme.presentation.components.ShopBuddyMood

class RuleBasedRuleFormatter : RuleFormatter {

    override fun format(
        result: RecommendationResult
    ): ShopBuddyAdvice {

        return when {

            RecommendationSuggestion.REDUCE_SUGAR in result.suggestions ->

                ShopBuddyAdvice(
                    mood = ShopBuddyMood.Warning,
                    title = "🍭 Weniger Zucker",
                    text = "Dein Einkauf enthält viele zuckerreiche Produkte. Vielleicht ergänzt du ihn mit einer gesünderen Alternative."
                )

            RecommendationSuggestion.ADD_FRUIT in result.suggestions ->

                ShopBuddyAdvice(
                    mood = ShopBuddyMood.Happy,
                    title = "🍎 Mehr Obst",
                    text = "Ein Stück Obst würde deinen Einkauf sinnvoll ergänzen."
                )

            RecommendationSuggestion.ADD_VEGETABLES in result.suggestions ->

                ShopBuddyAdvice(
                    mood = ShopBuddyMood.Happy,
                    title = "🥦 Mehr Gemüse",
                    text = "Etwas Gemüse sorgt für mehr Vielfalt in deinem Einkauf."
                )

            else ->

                ShopBuddyAdvice(
                    mood = ShopBuddyMood.Happy,
                    title = "✅ Gute Auswahl",
                    text = "Dein Einkauf wirkt bereits ausgewogen."
                )
        }
    }
}