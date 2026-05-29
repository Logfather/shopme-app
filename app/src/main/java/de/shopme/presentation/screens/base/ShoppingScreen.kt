package de.shopme.presentation.screens.base
//
//import androidx.compose.animation.AnimatedContent
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.material3.Scaffold
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import de.shopme.ui.components.button.ShopBuddyButton
//import de.shopme.ui.components.listitem.ShopBuddyListItem
//import de.shopme.ui.components.state.BagState
//import de.shopme.ui.components.state.resolveBagState
//
//@Composable
//fun ShoppingScreen(
//    items: List<ShoppingItemUi>,
//    onToggleItem: (ShoppingItemUi) -> Unit,
//    onAddItem: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//
//    val total = items.size
//    val checked = items.count { it.isChecked }
//
//    val bagState = resolveBagState(total, checked)
//
//    fun resolveBagState(total: Int, checked: Int): BagState {
//        return when {
//            total == 0 -> BagState.EMPTY
//            checked == total -> BagState.DONE
//            else -> BagState.ACTIVE
//        }
//    }
//
//    Scaffold(
//        modifier = modifier,
//        floatingActionButton = {
//            ShopBuddyButton(
//                text = "Add",
//                onClick = onAddItem
//            )
//        }
//    ) { padding ->
//
//        Column(
//            modifier = Modifier
//                .padding(padding)
//                .fillMaxSize()
//        ) {
//
//            // 🧩 HEADER
//            ShoppingHeader(
//                total = total,
//                checked = checked
//            )
//
//            // 🛍️ BAG STATE
//            AnimatedContent(targetState = bagState, label = "") { state ->
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 16.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    ShopBuddyBag(
//                        state = state,
//                        modifier = Modifier.size(140.dp)
//                    )
//                }
//            }
//
//            // 📋 LIST
//            LazyColumn(
//                modifier = Modifier.fillMaxSize(),
//                contentPadding = PaddingValues(bottom = 80.dp)
//            ) {
//                items(items, key = { it.id }) { item ->
//
//                    ShopBuddyListItem(
//                        name = item.name,
//                        subtitle = item.quantity,
//                        isChecked = item.isChecked,
//                        categoryColor = item.categoryColor,
//                        onCheckedChange = {
//                            onToggleItem(item)
//                        }
//                    )
//
//                    Spacer(modifier = Modifier.height(8.dp))
//                }
//            }
//        }
//    }
//}