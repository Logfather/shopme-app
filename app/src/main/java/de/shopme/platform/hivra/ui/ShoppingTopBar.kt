package de.shopme.platform.hivra.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingTopBar() {
    TopAppBar(
        title = {
            Text(
                text = "Hivra",
                style = MaterialTheme.typography.titleLarge
            )
        },
        actions = {
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.MoreVert, null)
            }
        }
    )
}