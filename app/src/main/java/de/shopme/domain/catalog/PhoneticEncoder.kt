package de.shopme.domain.catalog

class PhoneticEncoder {

    fun encode(input: String): String {
        val word = input.lowercase()

        val result = StringBuilder()

        for (c in word) {
            val code = when (c) {
                'a','e','i','j','o','u','y' -> "0"
                'h' -> ""
                'b','p' -> "1"
                'd','t' -> "2"
                'f','v','w' -> "3"
                'g','k','q' -> "4"
                'l' -> "5"
                'm','n' -> "6"
                'r' -> "7"
                's','z','ß','c','x' -> "8"
                else -> ""
            }

            result.append(code)
        }

        return result.toString()
    }
}