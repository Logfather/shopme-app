package de.shopme.ui.illustration.icons.bags

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.shopme.R

@Composable
fun ShoppingCartIcon(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    contentDescription: String = "Shopping cart",
    alpha: Float = 1f
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
    ) {
        Image(
            painter = painterResource(id = R.drawable.shopping_cart_icon),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha)
        )
    }
}