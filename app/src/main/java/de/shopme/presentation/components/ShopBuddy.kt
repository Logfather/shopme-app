package de.shopme.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import de.shopme.R

@Composable
fun ShopBuddy(
    mood: ShopBuddyMood,
    modifier: Modifier = Modifier
) {

    val imageRes = when (mood) {

        ShopBuddyMood.Idle ->
            R.drawable.shopbuddy_idle

        ShopBuddyMood.Listening ->
            R.drawable.shopbuddy_listening

        ShopBuddyMood.Happy ->
            R.drawable.shopbuddy_happy

        ShopBuddyMood.Thinking ->
            R.drawable.shopbuddy_thinking

        ShopBuddyMood.Warning ->
            R.drawable.shopbuddy_warning
    }

    Image(
        painter = painterResource(imageRes),
        contentDescription = "ShopBuddy",
        modifier = modifier
    )
}

enum class ShopBuddyMood {
    Idle,
    Listening,
    Happy,
    Thinking,
    Warning
}

data class ShopBuddyState(
    val mood: ShopBuddyMood,
    val text: String
)