package de.shopme.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import de.shopme.domain.model.ShoppingItem

@Composable
fun SupermarketItemRow(
    item: ShoppingItem,
    categoryColor: Color,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: (String) -> Unit
) {

    val brandOlive = MaterialTheme.colorScheme.secondary
    val anthracite = Color(0xFF2B2B2B)

    val isEditing = !item.isChecked

    var textFieldValue by remember(item.id) {
        mutableStateOf(
            TextFieldValue(
                text = item.name,
                selection = TextRange(item.name.length)
            )
        )
    }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isEditing) {
        if (isEditing) {
            focusRequester.requestFocus()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(brandOlive)
            .heightIn(min = 64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .width(6.dp)
                .fillMaxHeight()
                .background(categoryColor)
        )

        Spacer(Modifier.width(12.dp))

        Checkbox(
            checked = item.isChecked,
            onCheckedChange = {
                onToggle()
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
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = anthracite,
                    unfocusedContainerColor = anthracite,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(Modifier.width(8.dp))

            TextButton(
                onClick = {
                    onEdit(textFieldValue.text)
                    onToggle()
                }
            ) {
                Text("Fertig", color = Color.Black)
            }

        } else {

            Text(
                text = item.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondary
            )

            Spacer(Modifier.width(8.dp))

            TextButton(
                onClick = { onDelete() }
            ) {
                Text(
                    "Löschen",
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}