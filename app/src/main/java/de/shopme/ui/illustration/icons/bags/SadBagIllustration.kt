package de.shopme.ui.illustration.icons.bags

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.shopme.R
import de.shopme.ui.theme.BrandWhite

@Composable
fun SadBagIllustration(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                clip = false
            )
            .background(BrandWhite, CircleShape)
            .padding(2.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_shopme),
            contentDescription = stringResource(R.string.shopping_bag_illustration_description),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SadBagIllustrationPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        SadBagIllustration(
            modifier = Modifier.size(320.dp)
        )
    }
}
