package de.shopme.ui.illustration.icons.bags

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.shopme.R

@Composable
fun EmptyBagIcon(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFF5F5F5)), // neutraler UI-Hintergrund
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.empty_bag),
            contentDescription = "Empty sad bag",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = 0.95f
                }
        )
    }
}