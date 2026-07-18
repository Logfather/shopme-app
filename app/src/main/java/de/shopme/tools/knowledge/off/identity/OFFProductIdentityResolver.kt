package de.shopme.tools.knowledge.off.identity

class OFFProductIdentityResolver {

    private fun cleanup(
        value: String
    ): String {

        return value
            .trim()
            .trim(',', '-', '|')
            .trim()
    }

    fun resolve(
        productName: String,
        brand: String?,
        categories: String?
    ): String {

        val normalizedName =
            normalize(productName)

        val normalizedBrand =
            brand
                ?.let(::normalize)

        // Brand am Anfang entfernen:
        //
        // "Lagg's Green Tea"
        // -> "green tea"

        val withoutBrand =
            if (
                normalizedBrand != null &&
                normalizedName.startsWith(normalizedBrand)
            ) {
                normalizedName
                    .removePrefix(normalizedBrand)
                    .trim()
            } else {
                normalizedName
            }

        return cleanup(withoutBrand)
    }

    private fun normalize(
        value: String
    ): String {

        return value
            .trim()
            .lowercase()
            .replace("\\s+".toRegex(), " ")
    }
}