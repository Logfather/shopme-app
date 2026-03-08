package de.shopme.core.mapper

class QuantityMapper(
    private val map: Map<String, String>
) {

    private val numberWords = mapOf(
        "null" to 0,
        "ein" to 1,
        "eine" to 1,
        "einen" to 1,
        "eins" to 1,
        "zwei" to 2,
        "drei" to 3,
        "vier" to 4,
        "fünf" to 5,
        "sechs" to 6,
        "sieben" to 7,
        "acht" to 8,
        "neun" to 9,
        "zehn" to 10
    )

    fun normalize(input: String): String {

        var result = input.lowercase()

        // 🔹 1️⃣ Einheiten normalisieren (gramm → g etc.)
        map.forEach { (key, value) ->
            val regex = "\\b${Regex.escape(key)}\\b".toRegex()
            result = result.replace(regex, value)
        }

        // 🔹 2️⃣ Zahlwörter in Ziffern umwandeln
        numberWords.forEach { (word, number) ->
            val regex = "\\b$word\\b".toRegex()
            result = result.replace(regex, number.toString())
        }

        // 🔹 3️⃣ Mengenformate erkennen
        val quantityRegex = Regex(
            """^(\d+)\s*(x|mal|packung|packungen|stk|stück|stücke)?\s+(.+)""",
            RegexOption.IGNORE_CASE
        )

        val match = quantityRegex.find(result)

        if (match != null) {

            val amount = match.groupValues[1]
            val product = match.groupValues[3]

            result = "$amount x $product"
        }

        // 🔹 4️⃣ Leerzeichen bereinigen
        result = result
            .replace("\\s+".toRegex(), " ")
            .trim()

        // 🔹 5️⃣ Erste Buchstaben groß
        return result.replaceFirstChar { it.uppercase() }
    }
}