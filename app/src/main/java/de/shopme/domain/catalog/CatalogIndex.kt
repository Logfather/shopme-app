package de.shopme.domain.catalog

class CatalogIndex(
    items: List<CatalogItem>
) {

    private val phoneticIndex =
        mutableMapOf<String, CatalogItem>()

    private val autocompleteIndex =
        mutableMapOf<String, MutableList<CatalogItem>>()

    private val normalizedIndex =
        mutableMapOf<String, CatalogItem>()

    private val prefixIndex =
        mutableSetOf<String>()

    init {

        items.forEach { item ->

            normalizedIndex[item.normalized] = item

            val word = item.normalized

            for (i in 3..word.length) {
                prefixIndex.add(word.substring(0, i))
            }

            item.phonetic_tokens.forEach { token ->
                phoneticIndex[token] = item
            }

            item.autocomplete_tokens.forEach { token ->
                autocompleteIndex
                    .getOrPut(token) { mutableListOf() }
                    .add(item)
            }
        }
    }

    fun findByPhonetic(token: String): CatalogItem? =
        phoneticIndex[token]

    fun autocomplete(prefix: String): List<CatalogItem> {

        val q = prefix.lowercase()

        return normalizedIndex
            .values
            .asSequence()
            .filter { item ->
                item.normalized.startsWith(q)
            }
            .distinct()
            .sortedBy { it.production == "Bio" }
            .take(10)
            .toList()
    }

    fun normalize(word: String): CatalogItem? =
        normalizedIndex[word]

    fun hasPrefix(prefix: String): Boolean =
        prefixIndex.contains(prefix)
}