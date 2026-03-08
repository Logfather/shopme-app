package de.shopme.domain.model

import de.shopme.R

enum class StoreType(
    val displayName: String,
    val logoRes: Int
) {
    EDEKA("Edeka", R.drawable.edeka_icon),
    REWE("Rewe", R.drawable.rewe_icon),
    DM("dm", R.drawable.dm_icon),
    ALDI("Aldi", R.drawable.aldi_icon),
    MUELLER("Müller", R.drawable.mueller_icon),
    KAUFLAND("Kaufland", R.drawable.kaufland_icon),
    LIDL("Lidl", R.drawable.lidl_icon),
    NETTO("Netto", R.drawable.netto_icon),
    NORMA("Norma", R.drawable.norma_icon),
    ROSSMANN("Rossmann", R.drawable.rossmann_icon)
}