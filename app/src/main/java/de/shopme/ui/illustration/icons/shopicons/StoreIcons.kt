package de.shopme.ui.illustration.icons.shopicons

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.shopme.domain.model.StoreType

@Composable
fun StoreIcon(
    store: StoreType,
    modifier: Modifier = Modifier
) {
    when (store) {

        StoreType.ALDI ->
            AldiLogo(modifier = modifier)

        StoreType.DM ->
            DmLogo(modifier = modifier)

        StoreType.EDEKA ->
            EdekaLogo(modifier = modifier)

        StoreType.KAUFLAND ->
            KauflandLogo(modifier = modifier)

        StoreType.LIDL ->
            LidlLogo(modifier = modifier)

        StoreType.MUELLER ->
            MuellerLogo(modifier = modifier)

        StoreType.NETTO ->
            NettoLogo(modifier = modifier)

        StoreType.NORMA ->
            NormaLogo(modifier = modifier)

        StoreType.REWE ->
            ReweLogo(modifier = modifier)

        StoreType.ROSSMANN ->
            RossmannLogo(modifier = modifier)
    }
}