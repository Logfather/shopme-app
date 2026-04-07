package de.shopme.ui.illustration.icons.bags

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

import de.shopme.R

@Composable
fun HappyBagIllustration(
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.happy_bag),
        contentDescription = "Happy Shopping Bag",
        modifier = modifier
            .sizeIn(maxWidth = 450.dp, maxHeight = 450.dp)
            .aspectRatio(1f)
    )
}