package de.shopme.data.input.speech

import de.shopme.domain.service.CatalogService


data class ParsedSpeechItem(
    val name: String,
    val quantity: Int,
    val category: String?
)

class SpeechItemParser(
    private val catalogService: CatalogService
) {

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



    fun parseSpeech(text: String): List<ParsedSpeechItem> {

        val cleaned = normalize(text)

        val tokens =
            cleaned.split(" ")
                .map { it.trim() }
                .filter { it.isNotBlank() }

        val results = mutableListOf<ParsedSpeechItem>()

        var quantity = 1
        val buffer = mutableListOf<String>()

        var i = 0

        while (i < tokens.size) {

            val token = tokens[i]

            // ------------------------------------------------------------
            // Phrase-Erkennung für zusammengesetzte Produkte
            // ------------------------------------------------------------

            if (buffer.isNotEmpty()) {

                val phraseCandidate =
                    buffer.last() + " " + token

                val phraseMatch =
                    catalogService.resolveExactSpeech(phraseCandidate)

                if (phraseMatch != null) {

                    results.removeLastOrNull()

                    results.add(
                        ParsedSpeechItem(
                            name = phraseMatch.itemname,
                            quantity = quantity,
                            category = phraseMatch.category
                        )
                    )

                    buffer.clear()
                    quantity = 1
                    i++
                    continue
                }
            }

            val parsedQuantity =
                token.toIntOrNull()
                    ?: numberWords[token]

            if (
                parsedQuantity != null &&
                i + 1 < tokens.size
            ) {

                quantity = parsedQuantity

                i++
                continue
            }

            // ------------------------------------------------------------
            // NEU: Sliding Window Phrase Check (Bigram)
            // ------------------------------------------------------------

            if (buffer.isNotEmpty()) {

                val phrase =
                    buffer.last() + " " + token

                val phraseMatch =
                    catalogService.resolveExactSpeech(phrase)

                if (phraseMatch != null) {

                    results.removeLastOrNull()

                    results.add(
                        ParsedSpeechItem(
                            name = phraseMatch.itemname,
                            quantity = quantity,
                            category = phraseMatch.category
                        )
                    )

                    buffer.clear()
                    quantity = 1
                    i++
                    continue
                }
            }

            // 3️⃣ Einzelwort sofort prüfen
            val singleMatch =
                catalogService.resolveExactSpeech(token)

            if (singleMatch != null) {

                if (buffer.isNotEmpty()) {

                    buffer.forEach { word ->

                        results.add(
                            ParsedSpeechItem(
                                name = word.replaceFirstChar {
                                    it.uppercase()
                                },
                                quantity = quantity,
                                category = "Sonstiges"
                            )
                        )
                    }
                }

                results.add(
                    ParsedSpeechItem(
                        name = singleMatch.itemname,
                        quantity = quantity,
                        category = singleMatch.category
                    )
                )

                quantity = 1
                buffer.clear()
                i++
                continue
            }

            // 4️⃣ Phrase aufbauen
            buffer.add(token)

            val phrase = buffer.joinToString(" ")

            val match = catalogService.resolveExactSpeech(phrase)

            if (match != null) {

                results.add(
                    ParsedSpeechItem(
                        name = match.itemname,
                        quantity = quantity,
                        category = match.category
                    )
                )

                buffer.clear()
                quantity = 1

            } else {

                val compound = splitCompoundWord(phrase)

//                if (compound.isNotEmpty()) {
//
//                    compound.forEach { name ->
//
//                        val item =
//                            catalogService.resolveExactSpeech(name)
//
//                        if (item != null) {
//
//                            results.add(
//                                ParsedSpeechItem(
//                                    name = item.itemname,
//                                    quantity = quantity,
//                                    category = item.category
//                                )
//                            )
//                        }
//                    }
//
//                    buffer.clear()
//                    quantity = 1
//                    i++
//                    continue
//                }

                // ------------------------------------------------
                // NEU: Mehrfach-Items aus zusammengezogenem Wort
                // ------------------------------------------------

                val recovered = recoverMultipleItems(token)

                if (recovered.isNotEmpty()) {

                    recovered.forEach { name ->

                        val item = catalogService.resolveSpeech(name)

                        if (item != null) {

                            results.add(
                                ParsedSpeechItem(
                                    name = item.itemname,
                                    quantity = quantity,
                                    category = item.category
                                )
                            )
                        }
                    }

                    buffer.clear()
                    quantity = 1
                    i++
                    continue
                }
            }
            i++
        }
        if (buffer.isNotEmpty()) {

            buffer.forEach { word ->

                results.add(
                    ParsedSpeechItem(
                        name = word.replaceFirstChar {
                            it.uppercase()
                        },
                        quantity = quantity,
                        category = "Sonstiges"
                    )
                )
            }
        }
        buffer.clear()
        return aggregate(results)
    }

    private fun aggregate(items: List<ParsedSpeechItem>): List<ParsedSpeechItem> {

        val map = linkedMapOf<String, ParsedSpeechItem>()

        items.forEach { item ->

            val existing = map[item.name]

            if (existing == null) {

                map[item.name] = item

            } else {

                map[item.name] = existing.copy(
                    quantity = existing.quantity + item.quantity
                )
            }
        }

        return map.values.toList()
    }

    private fun normalize(text: String): String {
        return text
            .lowercase()
            .replace(",", " ")
            .replace(" und ", " ")
            .replace("bitte", "")
            .replace("noch", "")
            .replace("mal", "")
            .trim()
    }

    private fun splitCompoundWord(word: String): List<String> {

        val result = mutableListOf<String>()

        // mindestens 3 Zeichen pro Teil
        for (i in 3 until word.length - 2) {

            val left = word.substring(0, i)

            // NEU: schneller Prefix Check
            if (!catalogService.hasPrefix(left)) {
                continue
            }

            val right = word.substring(i)

            val leftMatch =
                catalogService.resolveExactSpeech(left)

            val rightMatch =
                catalogService.resolveExactSpeech(right)

            if (leftMatch != null && rightMatch != null) {

                result.add(leftMatch.itemname)
                result.add(rightMatch.itemname)

                return result
            }
        }

        return emptyList()
    }
    private fun recoverMultipleItems(word: String): List<String> {

        val result = mutableListOf<String>()

        val normalized = word.lowercase()

        for (i in 3 until normalized.length - 2) {

            val left = normalized.substring(0, i)
            val right = normalized.substring(i)

            val leftMatch =
                catalogService.resolveExactSpeech(left)

            val rightMatch =
                catalogService.resolveExactSpeech(right)

            if (leftMatch != null && rightMatch != null) {

                result.add(leftMatch.itemname)
                result.add(rightMatch.itemname)

                return result
            }

            // deutsches Fugen-S berücksichtigen
            if (right.startsWith("s") && right.length > 3) {

                val rightAlt = right.substring(1)

                val rightAltMatch =
                    catalogService.resolveExactSpeech(rightAlt)

                if (leftMatch != null && rightAltMatch != null) {

                    result.add(leftMatch.itemname)
                    result.add(rightAltMatch.itemname)

                    return result
                }
            }
        }

        return emptyList()
    }
}