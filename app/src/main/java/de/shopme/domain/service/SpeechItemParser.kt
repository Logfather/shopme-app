package de.shopme.domain.service

import de.shopme.data.input.speech.QuantityParser


data class ParsedSpeechItem(
    val name: String,
    val quantity: Int,
    val category: String
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

        val quantityTokens =
            QuantityParser.parse(tokens)

        val results = mutableListOf<ParsedSpeechItem>()

        var quantity = 1
        val buffer = mutableListOf<String>()

        quantityTokens.forEach { qt ->

            // ------------------------------------------------------------
            // Phrase-Erkennung für zusammengesetzte Produkte
            // ------------------------------------------------------------

            if (buffer.isNotEmpty()) {

                val phraseCandidate =
                    buffer.last() + " " + qt

                val phraseMatch =
                    catalogService.resolveSpeech(phraseCandidate)

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
                    return@forEach
                }
            }

            val token = qt.word
            quantity = qt.quantity

            // 1️⃣ Zahl
            if (token.all { it.isDigit() }) {
                quantity = token.toInt()
                return@forEach
            }

            // 2️⃣ Zahlwort
            numberWords[token]?.let {
                quantity = it
                return@forEach
            }

            // ------------------------------------------------------------
            // NEU: Sliding Window Phrase Check (Bigram)
            // ------------------------------------------------------------

            if (buffer.isNotEmpty()) {

                val phrase =
                    buffer.last() + " " + token

                val phraseMatch =
                    catalogService.resolveSpeech(phrase)

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
                    return@forEach
                }
            }

            // 3️⃣ Einzelwort sofort prüfen
            val singleMatch = catalogService.resolveSpeech(token)

            if (singleMatch != null) {

                results.add(
                    ParsedSpeechItem(
                        name = singleMatch.itemname,
                        quantity = quantity,
                        category = singleMatch.category
                    )
                )

                quantity = 1
                buffer.clear()
                return@forEach
            }

            // 4️⃣ Phrase aufbauen
            buffer.add(token)

            val phrase = buffer.joinToString(" ")

            val match = catalogService.resolveSpeech(phrase)

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

                if (compound.isNotEmpty()) {

                    compound.forEach { name ->

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
                    return@forEach
                }

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
                    return@forEach
                }
            }
        }
        if (buffer.isNotEmpty()) {

            val phrase = buffer.joinToString(" ")

            val match = catalogService.resolveSpeech(phrase)

            results.add(
                ParsedSpeechItem(
                    name = match?.itemname ?: phrase,
                    quantity = quantity,
                    category = match?.category ?: "Sonstiges"
                )
            )
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

    private fun parseSingle(segment: String): ParsedSpeechItem? {

        val tokens = segment.split(" ")

        var quantity = 1
        var nameStartIndex = 0

        val first = tokens.firstOrNull() ?: return null

        if (first.all { it.isDigit() }) {
            quantity = first.toInt()
            nameStartIndex = 1
        } else if (numberWords.containsKey(first)) {
            quantity = numberWords[first] ?: 1
            nameStartIndex = 1
        }

        val name = tokens
            .drop(nameStartIndex)
            .joinToString(" ")

        if (name.isBlank()) return null

        val match = catalogService.resolveSpeech(name)

        val resolvedName = match?.itemname ?: name
        val category = match?.category ?: "Sonstiges"

        return ParsedSpeechItem(
            name = resolvedName,
            quantity = quantity,
            category = category
        )
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

            val leftMatch = catalogService.resolveSpeech(left)
            val rightMatch = catalogService.resolveSpeech(right)

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

            val leftMatch = catalogService.resolveSpeech(left)
            val rightMatch = catalogService.resolveSpeech(right)

            if (leftMatch != null && rightMatch != null) {

                result.add(leftMatch.itemname)
                result.add(rightMatch.itemname)

                return result
            }

            // deutsches Fugen-S berücksichtigen
            if (right.startsWith("s") && right.length > 3) {

                val rightAlt = right.substring(1)

                val rightAltMatch =
                    catalogService.resolveSpeech(rightAlt)

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