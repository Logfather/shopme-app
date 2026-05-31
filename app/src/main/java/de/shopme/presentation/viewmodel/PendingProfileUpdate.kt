package de.shopme.presentation.viewmodel

data class PendingProfileUpdate(
    val nickName: String,
    val firstName: String?,
    val lastName: String?,
    val email: String?
)