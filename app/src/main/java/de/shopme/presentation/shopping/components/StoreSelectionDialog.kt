package de.shopme.presentation.shopping.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.shopme.R
import de.shopme.domain.model.StoreType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

private fun normalizeListName(input: String): String {

    var text = input.trim()

    val replacements = mapOf(
        "ae" to "ä",
        "oe" to "ö",
        "ue" to "ü",
        "Ae" to "Ä",
        "Oe" to "Ö",
        "Ue" to "Ü"
    )

    replacements.forEach { (from, to) ->
        text = text.replace(from, to)
    }

    return text.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase()
        else it.toString()
    }
}

@Composable
fun StoreSelectionDialog(
    selectedStores: List<StoreType>,
    existingStores: List<StoreType>,
    onToggle: (StoreType) -> Unit,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit
){

    var customListName by remember {
        mutableStateOf(TextFieldValue(""))
    }

    var customLists by remember {
        mutableStateOf<List<String>>(emptyList())
    }

    var showCustomInput by remember {
        mutableStateOf(false)
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    val existingNames =
        remember(customLists) {
            customLists.map { it.lowercase() }
        }

    val isDuplicate =
        customListName.text.trim().lowercase() in existingNames

    AlertDialog(
        onDismissRequest = { onDismiss() },

        confirmButton = {
            TextButton(
                onClick = {

                    val name =
                        customListName.text.trim()

                    var lists = customLists

                    if (name.isNotBlank() && !isDuplicate) {
                        lists = lists + name
                    }

                    onConfirm(lists)
                }
            ) {
                Text("Erstellen")
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        },

        title = {
            Text("Wo möchtest du heute einkaufen?")
        },

        text = {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ){

                // =============================
                // CUSTOM LISTS (oben anzeigen)
                // =============================

                customLists.forEach { name ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Image(
                            painterResource(id = R.drawable.store_icon76),
                            contentDescription = name,
                            modifier = Modifier.size(40.dp)
                        )

                        Spacer(Modifier.width(12.dp))

                        Text(
                            text = name,
                            modifier = Modifier.weight(1f)
                        )

                        RadioButton(
                            selected = true,
                            onClick = null
                        )
                    }
                }

                // =============================
                // BUTTON: Eigene Liste hinzufügen
                // =============================

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {

                            customListName = TextFieldValue("")
                            showCustomInput = true
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Image(
                        painterResource(id = R.drawable.store_icon76),
                        contentDescription = "Eigene Liste",
                        modifier = Modifier.size(40.dp)
                    )

                    Spacer(Modifier.width(12.dp))

                    Text("Eigene Liste hinzufügen")
                }

                // =============================
                // INPUT FELD
                // =============================

                AnimatedVisibility(
                    visible = showCustomInput,
                    enter =
                        expandVertically(
                            animationSpec = tween(250)
                        ) + fadeIn(
                            animationSpec = tween(250)
                        ),
                    exit =
                        shrinkVertically(
                            animationSpec = tween(200)
                        ) + fadeOut(
                            animationSpec = tween(200)
                        )
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        OutlinedTextField(
                            value = customListName,
                            onValueChange = { newValue ->

                                val normalized =
                                    normalizeListName(newValue.text)

                                customListName =
                                    newValue.copy(text = normalized)
                            },
                            placeholder = { Text("Name der Liste") },
                            isError = isDuplicate,
                            supportingText = {
                                if (isDuplicate) {
                                    Text("Liste existiert bereits")
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                        )

                        Spacer(Modifier.width(8.dp))

                        TextButton(
                            onClick = {

                                if (customListName.text.isNotBlank() && !isDuplicate) {

                                    customLists =
                                        customLists + customListName.text.trim()

                                    customListName = TextFieldValue("")
                                    showCustomInput = false
                                }

                            }
                        ) {
                            Text("Fertig")
                        }
                    }
                }

                LaunchedEffect(showCustomInput) {
                    if (showCustomInput) {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    }
                }

                // =============================
                // TRENNLINIE
                // =============================

                Divider()

                Spacer(modifier = Modifier.height(8.dp))

                // =============================
                // SUPERMÄRKTE
                // =============================

                val sortedStores =
                    StoreType.values()
                        .sortedBy { it !in existingStores }

                sortedStores.forEach { store ->

                    val alreadyExists = store in existingStores

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                enabled = !alreadyExists
                            ) {
                                onToggle(store)
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Image(
                            painter = painterResource(id = store.logoRes),
                            contentDescription = store.displayName,
                            modifier = Modifier.size(40.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(text = store.displayName)

                            if (alreadyExists) {
                                Text("bereits vorhanden")
                            }
                        }

                        RadioButton(
                            selected = selectedStores.contains(store),
                            onClick = if (alreadyExists) null else {
                                { onToggle(store) }
                            },
                            enabled = !alreadyExists
                        )
                    }
                }
            }
        }
    )
}