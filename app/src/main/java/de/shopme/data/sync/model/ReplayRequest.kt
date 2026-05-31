package de.shopme.data.sync.model

data class ReplayRequest(

    val replayId: String,

    val reason: ReplayReason,

    val timestamp: Long
)