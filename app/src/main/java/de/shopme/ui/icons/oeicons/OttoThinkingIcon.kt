package de.shopme.ui.icons.oeicons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.shopme.R

@Composable
fun OttoThinkingIcon(
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(R.drawable.otto_face_thinking),
        contentDescription = "Otto denkt angestrengt",
        modifier = modifier.size(32.dp),
        contentScale = ContentScale.Fit
    )
}