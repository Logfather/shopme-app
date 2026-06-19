package de.shopme.domain.recommendation

enum class RecommendationPriority(

    val priority: Int

) {

    CRITICAL(100),

    HIGH(75),

    MEDIUM(50),

    LOW(25)

}