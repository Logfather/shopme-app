package de.shopme.presentation.screens
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import de.shopme.presentation.state.ShoppingState
import de.shopme.ui.components.CartoonLoader
import de.shopme.ui.illustration.icons.bags.HappyBagIllustration
import de.shopme.ui.illustration.icons.bags.InviteBagIcon
import de.shopme.ui.theme.BrandBlack
import de.shopme.ui.theme.BrandGreen
import de.shopme.ui.theme.BrandOlive
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteScreen(
    state: ShoppingState,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    if (!state.showInviteDialog) return
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    ModalBottomSheet(
        onDismissRequest = onDecline,
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
            // ============================================================
            // HEADER (aligned with ChooseLists)
            // ============================================================
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
                    text = "Einladung",
                    style = MaterialTheme.typography.titleLarge,
                    color = BrandBlack
                )
                Spacer(Modifier.height(4.dp))
                val sender = state.inviteSenderName?.takeIf { it.isNotBlank() } ?: "Jemand"
                val listCount = state.inviteListIds.size
                val inviteText = buildAnnotatedString {
                    append("$sender hat dich zu ")
                    when (listCount) {
                        0 -> {
                            append("einer Einladung")
                        }
                        1 -> {
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append("einer Liste")
                            }
                        }
                        else -> {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("$listCount Listen")
                            }
                        }
                    }
                    append(" eingeladen")
                }
                Text(
                    text = inviteText,
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandBlack.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InviteBagIcon(
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = when (listCount) {
                            0 -> "Du kannst dieser Einladung beitreten"
                            1 -> "Du trittst dieser Liste bei"
                            else -> "Du trittst diesen Listen bei"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = BrandBlack.copy(alpha = 0.5f)
                    )
                }
                Spacer(Modifier.height(6.dp))
                if (listCount > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Gemeinsam einkaufen & Listen teilen",
                        style = MaterialTheme.typography.bodySmall,
                        color = BrandBlack.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            // ============================================================
            // STATE: LOADING
            // ============================================================
            if (state.isInviteLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CartoonLoader()
                }
                Text(
                    text = "Einladung wird geladen…",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(20.dp))
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Abbrechen", color = BrandBlack)
                }
                Spacer(Modifier.height(12.dp))
                return@Column
            }
            // ============================================================
            // STATE: ERROR
            // ============================================================
            state.inviteError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                )
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Schließen", color = BrandBlack)
                }
                Spacer(Modifier.height(12.dp))
                return@Column
            }
            // ============================================================
            // LIST PREVIEW (aligned style)
            // ============================================================
            val lists = state.inviteResolvedLists
            if (lists.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CartoonLoader()
                }
                Text(
                    text = "Listen werden geladen…",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                ) {
                    lists.forEach { list ->
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HappyBagIllustration(
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = list.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = BrandBlack
                                    )
                                    Text(
                                        text = "${list.itemCount} Artikel",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BrandBlack.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            Divider(color = BrandBlack.copy(alpha = 0.08f))
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            // ============================================================
            // ACTIONS (ChooseLists-like behavior)
            // ============================================================
            if (state.isJoining) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CartoonLoader()
                }
            } else {
                val enabled = !state.inviteResolvedLists.isNullOrEmpty()
                if (!enabled) {
                    Text(
                        text = "Warte auf gültige Listen…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrandBlack.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                } else {
                    Button(
                        onClick = onAccept,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandGreen,
                            contentColor = BrandBlack
                        )
                    ) {
                        Text("Beitreten")
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Abbrechen", color = BrandBlack)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
