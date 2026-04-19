package de.shopme.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import de.shopme.domain.model.ShoppingItem
import de.shopme.domain.model.SyncStatus
import de.shopme.presentation.mapper.toUiState
import de.shopme.presentation.model.SyncUiState
import de.shopme.ui.theme.BrandBlack
import de.shopme.ui.theme.BrandOlive

@Composable
fun SupermarketItemRow(
    item: ShoppingItem,
    categoryColor: Color,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onRetry: (String) -> Unit,
    onUpdate: (String) -> Unit
){

    var isEditing by remember(item.id) {
        mutableStateOf(!item.isChecked)
    }

    var textFieldValue by rememberSaveable(item.id, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(item.name))
    }

    LaunchedEffect(item.id, item.name) {
        if (!isEditing && textFieldValue.text != item.name) {
            textFieldValue = TextFieldValue(item.name)
        }
    }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isEditing) {
        if (isEditing) {
            focusRequester.requestFocus()
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(BrandOlive),


        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .width(6.dp)
                .height(40.dp)
                .background(categoryColor)
        )

        Spacer(Modifier.width(12.dp))

        Checkbox(
            checked = item.isChecked,
            onCheckedChange = { checked ->
                onToggle(checked)

                if (!checked) {
                    isEditing = true   // nur öffnen wenn unchecked
                }
            }
        )

        Spacer(Modifier.width(12.dp))

        if (isEditing) {

            TextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                singleLine = true
            )

            Spacer(Modifier.width(8.dp))

            TextButton(
                onClick = {

                    val newText = textFieldValue.text.trim()

                    if (newText.isNotBlank()) {
                        textFieldValue = TextFieldValue(
                            text = newText,
                            selection = TextRange(newText.length)
                        )
                    }

                    onUpdate(newText)
                    isEditing = false
                    keyboardController?.hide()
                }
            ) {
                Text(
                    text = "Fertig",
                    style = MaterialTheme.typography.bodyLarge,
                    color = BrandBlack
                )
            }
        } else {

            Text(
                text = textFieldValue.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(8.dp))

            TextButton(
                onClick = { onDelete() }
            ) {
                Text(
                    text = "Löschen",
                    color = BrandBlack,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))


        Box(modifier = Modifier.size(24.dp)) {

            val stableStatus = item.syncStatus

            val uiState = remember(stableStatus) {
                stableStatus.toUiState()
            }
            SyncStatusIcon(
                state = uiState,
                onRetry = {
                    onRetry(item.id)
                }
            )
        }
    }
}