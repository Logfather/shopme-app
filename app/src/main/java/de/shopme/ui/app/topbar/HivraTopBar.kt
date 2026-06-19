package de.shopme.ui.app.topbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.shopme.ui.theme.BrandBlack
import de.shopme.ui.theme.BrandGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HivraTopBar(

    hasProfile: Boolean,

    displayName: String?,

    hasLists: Boolean,

    onProfile: () -> Unit,

    onFoodIntelligence: () -> Unit,

    onBuildReport: () -> Unit,

    onShare: () -> Unit

){

    var showMenu by remember {

        mutableStateOf(false)

    }

    CenterAlignedTopAppBar(

        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(

            containerColor = BrandGreen,

            titleContentColor = BrandBlack

        ),

        navigationIcon = {

            Box {

                Row(

                    verticalAlignment = Alignment.CenterVertically,

                    modifier = Modifier

                        .clickable {

                            showMenu = true

                        }

                        .padding(start = 8.dp)

                ) {

                    Icon(

                        imageVector = Icons.Default.AccountCircle,

                        contentDescription = null,

                        tint = BrandBlack

                    )

                    Spacer(

                        modifier = Modifier.width(6.dp)

                    )

                    Text(

                        text = if (!hasProfile) {

                            "Einstellungen"

                        } else {

                            displayName ?: ""

                        },

                        color = BrandBlack,

                        maxLines = 1

                    )

                }

                HivraMenu(

                    expanded = showMenu,

                    onDismiss = {

                        showMenu = false

                    },

                    onProfile = {

                        showMenu = false

                        onProfile()

                    },

                    onFoodIntelligence = {

                        showMenu = false

                        onFoodIntelligence()

                    },

                    onBuildReport = {

                        showMenu = false

                        onBuildReport()

                    }

                )

            }

        },

        title = {

            Box(

                modifier = Modifier.fillMaxWidth(),

                contentAlignment = Alignment.CenterStart

            ) {

                // Hivra Logo

            }

        },

        actions = {

            if (hasLists) {

                Row(

                    verticalAlignment = Alignment.CenterVertically,

                    horizontalArrangement = Arrangement.Center,

                    modifier = Modifier

                        .clickable {

                            onShare()

                        }

                        .padding(horizontal = 12.dp, vertical = 8.dp)

                ) {

                    Text(

                        text = "Teilen",

                        color = BrandBlack,

                        maxLines = 1

                    )

                    Spacer(

                        modifier = Modifier.width(6.dp)

                    )

                    Icon(

                        imageVector = Icons.Default.Share,

                        contentDescription = "Listen teilen",

                        tint = BrandBlack

                    )

                }

            }

        }

    )

}