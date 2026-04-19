package de.shopme.presentation.screens

import android.R
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.shopme.ui.illustration.icons.bags.HappyBagIllustration
import de.shopme.ui.theme.BrandBlack
import de.shopme.ui.theme.BrandGreen
import de.shopme.ui.theme.BrandGrey
import de.shopme.ui.theme.BrandOlive
import de.shopme.ui.theme.BrandRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    mode: ProfileMode,
    currentName: String?,
    hasProfile: Boolean,
    firstNameValue: String?,
    lastNameValue: String?,
    emailValue: String?,
    isGoogleUser: Boolean,
    onCreateProfile: (String, String, String, String) -> Unit,
    onUpdateProfile: (String, String?, String?, String?) -> Unit,
    onGoogleSignIn: () -> Unit,
    onDismiss: () -> Unit,
    onShowSaveChoice: (String, String?, String?, String?) -> Unit,
    onUnlinkGoogle: () -> Unit,
    onLinkGoogle: () -> Unit,
    onDeleteAccount: () -> Unit

) {

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val textState = remember(currentName) {
        TextFieldValue(
            text = currentName ?: "",
            selection = TextRange((currentName ?: "").length)
        )
    }

    var nickNameState by remember(currentName) {
        mutableStateOf(textState)
    }

    var name by remember {
        mutableStateOf(currentName ?: "")
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    val nickName = nickNameState.text

    var firstName by remember(firstNameValue) {
        mutableStateOf(firstNameValue ?: "")
    }

    var lastName by remember(lastNameValue) {
        mutableStateOf(lastNameValue ?: "")
    }

    var email by remember(emailValue) {
        mutableStateOf(emailValue ?: "")
    }

    var showUnlinkDialog by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }

    val isValid = firstName.isNotBlank()
            && lastName.isNotBlank()
            && email.isNotBlank()
            && nickName.isNotBlank()

    val isNickNameValid = nickName.isNotBlank()
    val initialNick = currentName ?: ""
    val initialFirst = firstNameValue ?: ""
    val initialLast = lastNameValue ?: ""
    val initialEmail = emailValue ?: ""

    val isChanged by remember(
        nickNameState,
        firstName,
        lastName,
        email,
        currentName,
        firstNameValue,
        lastNameValue,
        emailValue
    ) {
        derivedStateOf {

            val nickChanged =
                nickNameState.text.trim() != (currentName ?: "").trim()

            val firstChanged =
                firstName.trim() != (firstNameValue ?: "").trim()

            val lastChanged =
                lastName.trim() != (lastNameValue ?: "").trim()

            val emailChanged =
                email.trim() != (emailValue ?: "").trim()

            nickChanged || firstChanged || lastChanged || emailChanged
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = BrandOlive,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandOlive)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {

            // ---------------- HEADER ----------------

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {

                val animationTrigger = remember(currentName) { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    animationTrigger.value = true
                }

                val scale by animateFloatAsState(
                    targetValue = if (animationTrigger.value) 1f else 0.8f,
                    animationSpec = spring(dampingRatio = 0.6f)
                )

                HappyBagIllustration(
                    modifier = Modifier
                        .size(50.dp)
                        .scale(scale)
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Dein Profil",
                    style = MaterialTheme.typography.titleLarge,
                    color = BrandBlack
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Profil wird zum Teilen benötigt",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = BrandBlack
                )
            }

            Spacer(Modifier.height(10.dp))

            // ---------------- INPUTS ----------------

            Text(
                text = "Wie willst du genannt werden?",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = BrandBlack
            )

            OutlinedTextField(
                value = nickNameState,
                onValueChange = { nickNameState = it },
                label = { Text("Profilname") },
                isError = nickName.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )

            if (nickName.isBlank()) {
                Text(
                    text = "Profilname ist erforderlich",
                    color = BrandBlack,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(5.dp))

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(BrandGrey)
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text(
                    text = "Vorname",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Left,
                    color = BrandGrey
                ) },
                modifier = Modifier.fillMaxWidth(),

                colors = TextFieldDefaults.colors(

                    focusedContainerColor = BrandOlive,
                    unfocusedContainerColor = BrandOlive,
                    focusedIndicatorColor = BrandGrey,
                    unfocusedIndicatorColor = BrandGrey,
                    cursorColor = BrandBlack,
                    focusedTextColor = BrandBlack,
                    unfocusedTextColor = BrandBlack,
                    focusedLabelColor = BrandGrey,
                    unfocusedLabelColor = BrandGrey
                )
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text(
                    text = "Name",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Left,
                    color = BrandGrey
                ) },
                modifier = Modifier.fillMaxWidth(),

                colors = TextFieldDefaults.colors(

                    focusedContainerColor = BrandOlive,
                    unfocusedContainerColor = BrandOlive,
                    focusedIndicatorColor = BrandGrey,
                    unfocusedIndicatorColor = BrandGrey,
                    cursorColor = BrandBlack,
                    focusedTextColor = BrandBlack,
                    unfocusedTextColor = BrandBlack,
                    focusedLabelColor = BrandGrey,
                    unfocusedLabelColor = BrandGrey
                )
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    if (!isGoogleUser) {
                        email = it
                    }
                },
                enabled = !isGoogleUser,
                readOnly = isGoogleUser,
                label = {
                    Text(
                        text = "E-Mail",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Left,
                        color = BrandGrey
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = BrandOlive,
                    unfocusedContainerColor = BrandOlive,
                    disabledContainerColor = BrandOlive,

                    focusedIndicatorColor = BrandGrey,
                    unfocusedIndicatorColor = BrandGrey,
                    disabledIndicatorColor = BrandGrey,

                    cursorColor = BrandBlack,

                    focusedTextColor = BrandBlack,
                    unfocusedTextColor = BrandBlack,
                    disabledTextColor = BrandGrey,

                    focusedLabelColor = BrandGrey,
                    unfocusedLabelColor = BrandGrey,
                    disabledLabelColor = BrandGrey
                )
            )

            Spacer(Modifier.height(15.dp))

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(BrandGrey)
            )

            Spacer(Modifier.height(15.dp))

            // ---------------- CREATE MODE ----------------

            if (mode == ProfileMode.CREATE) {

                OutlinedButton(
                    onClick = {
                        if (isValid) {
                            onCreateProfile(firstName, lastName, email, nickName)
                        }
                    },
                    enabled = isValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isValid) BrandGreen else BrandOlive,
                        contentColor = if (isValid) BrandBlack else BrandOlive
                    ),
                    border = BorderStroke(
                        width = 2.dp,
                        color = BrandGrey
                    )
                ) {
                    Text(
                        text = "Mit Profil anmelden",
                        color = if (isValid) BrandBlack else BrandGrey
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "oder",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        onCreateProfile(firstName, lastName, email, nickName)
                        onGoogleSignIn()
                    },
                    enabled = isNickNameValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isNickNameValid) BrandGreen else BrandOlive,
                        contentColor = if (isNickNameValid) BrandBlack else BrandOlive
                    ),
                    border = BorderStroke(
                        width = 2.dp,
                        color = BrandGrey
                    )
                ) {
                    Text(
                        text = "Mit Google anmelden",
                        color = if (isNickNameValid) BrandBlack else BrandGrey
                    )
                }
            }

            // ---------------- EDIT MODE ----------------

            if (mode == ProfileMode.EDIT) {

                Column {

                    // ---------------- SPEICHERN ----------------

                    OutlinedButton(
                        onClick = {
                            if (nickName.isNotBlank()) {
                                onShowSaveChoice(
                                    nickName,
                                    firstName,
                                    lastName,
                                    email
                                )
                            }
                        },
                        enabled = nickName.isNotBlank() && isChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isChanged) BrandGreen else BrandOlive,
                            contentColor = if (isChanged) BrandBlack else BrandOlive
                        ),
                        border = BorderStroke(
                            width = 2.dp,
                            color = BrandGrey
                        )
                    ) {
                        Text(
                            text = "Speichern",
                            color = if (isChanged) BrandBlack else BrandGrey
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // ---------------- ABBRECHEN ----------------

                    OutlinedButton(
                        onClick = {
                            onDismiss()
                        },
                        enabled = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = BrandGreen,
                            contentColor = BrandBlack
                        ),
                        border = BorderStroke(
                            width = 2.dp,
                            color = BrandGrey
                        )
                    ) {
                        Text(
                            text = "Abbrechen",
                            color = BrandBlack
                        )
                    }

                    Spacer(Modifier.height(10.dp))

// ---------------- GOOGLE ACCOUNT ACTION ----------------

                    if (isGoogleUser) {

                        OutlinedButton(
                            onClick = { showUnlinkDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = BrandRed,
                                contentColor = BrandBlack
                            ),
                            border = BorderStroke(2.dp, BrandGrey)
                        ) {
                            Text("Google Konto entfernen")
                        }

                    } else {

                        OutlinedButton(
                            onClick = { showLinkDialog = true },
                            enabled = nickName.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (nickName.isNotBlank()) BrandGreen else BrandOlive,
                                contentColor = BrandBlack
                            ),
                            border = BorderStroke(2.dp, BrandGrey)
                        ) {
                            Text("Mit Google verknüpfen")
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = BrandRed,
                        contentColor = BrandBlack
                    ),
                    border = BorderStroke(2.dp, BrandGrey)
                ) {
                    Text("Profil löschen")
                }
            }

            Spacer(Modifier.height(10.dp))

            // ---------------- VALIDATION ----------------

            if (!isValid) {
                Text(
                    text = "Bitte alle Felder ausfüllen",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = BrandBlack
                )
            }
        }

        // ---------------- GOOGLE LINKING ----------------

        if (showUnlinkDialog) {
            AlertDialog(
                onDismissRequest = { showUnlinkDialog = false },
                containerColor = BrandOlive,
                title = {
                    Text("Google entfernen", color = BrandBlack)
                },
                text = {
                    Text(
                        "Möchtest du dein Google Konto wirklich entfernen?",
                        color = BrandBlack
                    )
                },
                confirmButton = {
                    OutlinedButton(
                        onClick = {
                            showUnlinkDialog = false
                            onUnlinkGoogle()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandRed,
                            contentColor = BrandBlack
                        )
                    ) {
                        Text("Entfernen")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showUnlinkDialog = false }
                    ) {
                        Text("Abbrechen")
                    }
                }
            )
        }

        if (showLinkDialog) {
            AlertDialog(
                onDismissRequest = { showLinkDialog = false },
                containerColor = BrandOlive,
                title = {
                    Text("Mit Google verknüpfen", color = BrandBlack)
                },
                text = {
                    Text(
                        "Dein Konto wird mit Google verbunden.",
                        color = BrandBlack
                    )
                },
                confirmButton = {
                    OutlinedButton(
                        onClick = {
                            showLinkDialog = false
                            onLinkGoogle()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandGreen,
                            contentColor = BrandBlack
                        )
                    ) {
                        Text("Fortfahren")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showLinkDialog = false }
                    ) {
                        Text("Abbrechen")
                    }
                }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                containerColor = BrandOlive,
                title = {
                    Text("Konto löschen", color = BrandBlack)
                },
                text = {
                    Text(
                        "Dein Konto und alle Daten werden dauerhaft gelöscht.",
                        color = BrandBlack
                    )
                },
                confirmButton = {
                    OutlinedButton(
                        onClick = {
                            showDeleteDialog = false
                            onDeleteAccount()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandRed,
                            contentColor = BrandBlack
                        ),
                        border = BorderStroke(2.dp, BrandGrey)
                    ) {
                        Text("Endgültig löschen")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDeleteDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandGreen,
                            contentColor = BrandBlack
                        ),
                        border = BorderStroke(2.dp, BrandGrey)
                    ) {
                        Text("Abbrechen")
                    }
                }
            )
        }
    }
}

enum class ProfileMode {
    CREATE,
    EDIT
}