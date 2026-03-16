package de.shopme.domain.model

import de.shopme.R

enum class StoreType(
    val displayName: String
) {
    EDEKA("Edeka"),
    REWE("Rewe"),
    DM("dm"),
    ALDI("Aldi"),
    MUELLER("Müller"),
    KAUFLAND("Kaufland"),
    LIDL("Lidl"),
    NETTO("Netto"),
    NORMA("Norma"),
    ROSSMANN("Rossmann")
}