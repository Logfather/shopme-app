package de.shopme.ui.icons

enum class TrafficLight(

    val icon: String,
    val displayName: String

) {

    GREEN("🟢", "Sehr gut"),
    LIGHT_GREEN("🟩", "Gut"),
    YELLOW("🟡", "Mittel"),
    ORANGE("🟠", "Erhöht"),
    RED("🔴", "Hoch")

}