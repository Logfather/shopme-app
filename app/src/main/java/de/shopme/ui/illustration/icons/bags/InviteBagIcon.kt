package de.shopme.ui.illustration.icons.bags

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import de.shopme.R

@Composable
fun InviteBagIcon(
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.invite_bag),
        contentDescription = "Invite Icon",
        modifier = modifier
    )
}