package de.shopme.data.sync.util

import java.util.UUID

object ReplayIdGenerator {

    fun newReplayId(): String {

        return UUID
            .randomUUID()
            .toString()
            .take(8)
    }
}