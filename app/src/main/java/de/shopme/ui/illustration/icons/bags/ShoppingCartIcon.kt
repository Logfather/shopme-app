package de.shopme.ui.illustration.icons.bags

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.shopme.R

@Composable
fun ShoppingCartIcon(
    modifier: Modifier = Modifier,
    contentDescription: String? = "Shopping Cart"
) {
    Image(
        painter = painterResource(id = R.drawable.shopping_cart_icon),
        contentDescription = contentDescription,
        modifier = modifier
            .size(48.dp), // Standard Icon Size, anpassbar
        contentScale = ContentScale.Fit
    )
}