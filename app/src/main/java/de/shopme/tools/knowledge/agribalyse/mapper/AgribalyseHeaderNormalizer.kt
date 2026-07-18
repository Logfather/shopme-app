package de.shopme.tools.knowledge.agribalyse.mapper

class AgribalyseHeaderNormalizer {

    fun normalize(
        categoryHeader: List<String>,
        fieldHeader: List<String>
    ): List<String> {
        val maxSize = maxOf(
            categoryHeader.size,
            fieldHeader.size
        )

        return (0 until maxSize)
            .map { index ->
                normalizeHeader(
                    category = categoryHeader.getOrElse(index) { "" },
                    field = fieldHeader.getOrElse(index) { "" }
                )
            }
            .let(::makeUnique)
    }

    private fun normalizeHeader(
        category: String,
        field: String
    ): String {
        val cleanCategory = category.cleanHeader()
        val cleanField = field.cleanHeader()

        return when {
            cleanCategory.isBlank() -> cleanField
            cleanField.isBlank() -> cleanCategory
            cleanCategory == cleanField -> cleanField
            else -> "$cleanCategory | $cleanField"
        }
    }

    private fun makeUnique(
        headers: List<String>
    ): List<String> {
        val counts = mutableMapOf<String, Int>()

        return headers.map { header ->
            val count = counts.getOrDefault(header, 0) + 1
            counts[header] = count

            if (count == 1) {
                header
            } else {
                "$header ($count)"
            }
        }
    }

    private fun String.cleanHeader(): String =
        replace('\u00A0', ' ')   // No-Break Space
            .replace('\u202F', ' ') // Narrow No-Break Space
            .replace('\u2007', ' ') // Figure Space
            .replace('\u2060', ' ') // Word Joiner
            .replace("\r", "")
            .replace("\n", " ")
            .replace("\t", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
}