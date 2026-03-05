package de.shopme.domain.model

enum class StoreType(
    val displayName: String,
    val logoRes: Int
) {
    EDEKA("Edeka", de.shopme.R.drawable.edeka_icon),
    REWE("Rewe", de.shopme.R.drawable.rewe_icon),
    DM("dm", de.shopme.R.drawable.dm_icon),
    ALDI("Aldi", de.shopme.R.drawable.aldi_icon),
    MUELLER("Müller", de.shopme.R.drawable.mueller_icon),
    KAUFLAND("Kaufland", de.shopme.R.drawable.kaufland_icon),
    LIDL("Lidl", de.shopme.R.drawable.lidl_icon),
    NETTO("Netto", de.shopme.R.drawable.netto_icon),
    NORMA("Norma", de.shopme.R.drawable.norma_icon),
    ROSSMANN("Rossmann", de.shopme.R.drawable.rossmann_icon)
}