package de.shopme.presentation.screens

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.shopme.ui.illustration.icons.bags.AnimatedEmptyBagIcon
import de.shopme.ui.theme.BrandBlack
import de.shopme.ui.theme.BrandOlive
import de.shopme.ui.theme.BrandWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAllListsScreen(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BrandOlive,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {

            // ---------------- HEADER ----------------

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {

                val scale by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = 0.6f)
                )

                // 👉 gleiche Illustration → aber semantisch anders genutzt
                AnimatedEmptyBagIcon(
                    modifier = Modifier
                        .size(280.dp)
                        .scale(scale)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Alle Listen löschen",
                    style = MaterialTheme.typography.titleMedium,
                    color = BrandBlack
                )


                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Diese Aktion löscht alle deine Listen dauerhaft. Die Aktion kann nicht rückgängig gemacht werden!",
                    style = MaterialTheme.typography.titleLarge,
                    color = BrandBlack.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(24.dp))

            // ---------------- PRIMARY CTA ----------------

            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB00020), // Material Error Red
                    contentColor = BrandWhite
                )

            ) {
                Log.d("DELETE_BUTTON", "DeleteAllListsScreen - Button: Alle löschen")
                Text("Alle löschen")
            }

            Spacer(Modifier.height(12.dp))

            // ---------------- SECONDARY CTA ----------------

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Abbrechen", color = BrandBlack)
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}