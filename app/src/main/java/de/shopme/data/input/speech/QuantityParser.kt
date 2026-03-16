package de.shopme.data.input.speech

data class QuantityToken(
    val word: String,
    val quantity: Int
)

object QuantityParser {

    private val numberWords = mapOf(
        "ein" to 1,
        "eins" to 1,
        "eine" to 1,
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

    fun parse(tokens: List<String>): List<QuantityToken> {

        val result = mutableListOf<QuantityToken>()

        var i = 0

        while (i < tokens.size) {

            val token = tokens[i]

            val quantity =
                token.toIntOrNull()
                    ?: numberWords[token]

            if (quantity != null && i + 1 < tokens.size) {

                result.add(
                    QuantityToken(
                        word = tokens[i + 1],
                        quantity = quantity
                    )
                )

                i += 2
            }
            else {

                result.add(
                    QuantityToken(
                        word = token,
                        quantity = 1
                    )
                )

                i++
            }
        }

        return result
    }
}