package de.shopme.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import de.shopme.domain.model.SyncStatus
import de.shopme.presentation.model.SyncUiState
import androidx.compose.runtime.getValue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.shopme.ui.theme.BrandGreen
import de.shopme.ui.theme.BrandGrey

@Composable
fun SyncStatusIcon(
    state: SyncUiState,
    onRetry: (() -> Unit)? = null
) {
    when (val status = state.status) {

        is SyncStatus.Pending -> PendingIcon()

        is SyncStatus.Syncing -> SyncingIcon(
            progress = status.progress
        )

        is SyncStatus.Synced -> SuccessIcon()

        is SyncStatus.Failed -> FailedIcon(
            onRetry = if (status.canRetry) onRetry else null
        )
    }
}

@Composable
fun SyncingIcon(
    progress: Float?
) {

    if (progress != null) {

        CartoonLoader(progress = progress)

    } else {

        val infiniteTransition = rememberInfiniteTransition(label = "sync")

        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                tween(1000, easing = LinearEasing)
            ),
            label = "rotation"
        )

        Icon(
            imageVector = Icons.Rounded.Sync,
            contentDescription = "Syncing",
            modifier = Modifier
                .size(20.dp)
                .rotate(rotation)
        )
    }
}

@Composable
fun PendingIcon() {
    Icon(
        imageVector = Icons.Rounded.Schedule,
        contentDescription = "Pending",
        tint = BrandGrey,
        modifier = Modifier.size(20.dp)
    )
}

@Composable
fun SuccessIcon() {
    Icon(
        imageVector = Icons.Rounded.CheckCircle,
        contentDescription = "Synced",
        tint = Color(0xFF2E7D32),
        modifier = Modifier.size(20.dp)
    )
}

//@Composable
//fun FailedIcon(
//    onRetry: (() -> Unit)?
//) {
//    Icon(
//        imageVector = Icons.Rounded.CloudOff,
//        contentDescription = "Failed",
//        tint = Color(0xFFD32F2F),
//        modifier = Modifier
//            .size(20.dp)
//            .then(
//                if (onRetry != null) {
//                    Modifier.clickable { onRetry() }
//                } else {
//                    Modifier
//                }
//            )
//    )
//}

@Composable
fun FailedIcon(
    onRetry: (() -> Unit)?
) {
    IconButton(onClick = { onRetry?.invoke() }) {
        Icon(
            imageVector = Icons.Rounded.Refresh,
            contentDescription = "Retry",
            tint = Color(0xFFD32F2F)
        )
    }
}