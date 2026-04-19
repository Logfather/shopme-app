package de.shopme.domain.model

data class InviteData(
    val listIds: List<String>,
    val senderName: String,
    val createdAt: Long,
    val consumedAt: Long?
)