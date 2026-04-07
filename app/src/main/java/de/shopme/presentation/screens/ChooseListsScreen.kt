package de.shopme.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.shopme.domain.model.ShoppingList
import de.shopme.ui.illustration.icons.bags.HappyBagIllustration
import de.shopme.ui.theme.BrandGreen
import de.shopme.ui.theme.BrandOlive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseListsScreen(
    lists: List<ShoppingList>,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val selectedIds = remember { mutableStateListOf<String>() }

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

                HappyBagIllustration(
                    modifier = Modifier
                        .size(28.dp)
                        .scale(scale)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Listen teilen",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Wähle eine oder mehrere Listen",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(16.dp))

            // ---------------- LISTEN ----------------

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
            ) {

                lists.forEach { list ->

                    val isSelected = list.id in selectedIds

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isSelected) {
                                    selectedIds.remove(list.id)
                                } else {
                                    selectedIds.add(list.id)
                                }
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = {
                                if (it) {
                                    selectedIds.add(list.id)
                                } else {
                                    selectedIds.remove(list.id)
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = BrandGreen
                            )
                        )

                        Spacer(Modifier.width(10.dp))

                        Text(
                            text = list.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black
                        )
                    }

                    Divider(color = Color.Black.copy(alpha = 0.08f))
                }
            }

            Spacer(Modifier.height(20.dp))

            // ---------------- CTA LOGIC ----------------

            if (selectedIds.isEmpty()) {

                // 👉 Zustand 1: Kein Auswahl → Hinweis
                Text(
                    text = "Bitte mindestens 1 Liste wählen",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                )

            } else {

                // 👉 Zustand 2: Auswahl vorhanden → Button erscheint
                Button(
                    onClick = {
                        onConfirm(selectedIds.toList())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandGreen,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Teilen")
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Abbrechen", color = Color.Black)
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}