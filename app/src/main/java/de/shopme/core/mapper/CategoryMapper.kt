package de.shopme.core.mapper

import de.shopme.util.CategoryConfig
import java.text.Normalizer
import kotlin.collections.iterator

class CategoryMapper(
    config: CategoryConfig
) {

    private val productToCategory: Map<String, String>

    init {


        val map = mutableMapOf<String, String>()

        config.categories.forEach { group ->
            group.products.forEach { product ->
                map[normalize(product)] = group.name
            }
        }

        productToCategory = map
    }

    fun resolve(name: String): String {

        // 1️⃣ Mengenangabe entfernen (robuster)
        val withoutQuantity = name
            .lowercase()
            .replace(Regex("^\\d+\\s*(x)?\\s+"), "")

        val cleaned = normalize(withoutQuantity)

        // 2️⃣ Exakter Treffer
        productToCategory[cleaned]?.let {
            return it
        }

        // 3️⃣ Teiltreffer
        for ((product, category) in productToCategory) {
            if (cleaned.contains(product)) {
                return category
            }
        }

        return "Sonstiges"
    }

    private fun normalize(input: String): String {

        val lower = input.lowercase()

        val umlautNormalized = Normalizer
            .normalize(lower, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")

        return umlautNormalized
            .replace("[^a-z0-9 ]".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }
}