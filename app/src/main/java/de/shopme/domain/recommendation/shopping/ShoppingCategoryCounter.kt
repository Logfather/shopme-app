package de.shopme.domain.recommendation.shopping

import de.shopme.domain.recommendation.ShoppingContext

class ShoppingCategoryCounter {

    fun count(
        context: ShoppingContext
    ): Int {

        return context.categories
            .distinct()
            .size

    }

}