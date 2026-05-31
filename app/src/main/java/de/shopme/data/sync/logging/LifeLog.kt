package de.shopme.data.sync.logging

import android.util.Log

object LifeLog {

    private const val TAG = "LIFE_EVENT"

    fun event(message: String) {

        Log.d(
            TAG,
            message
        )
    }

    // ------------------------------------------------------------
    // ITEM EVENTS
    // ------------------------------------------------------------

    fun item(message: String) {

        event(
            "ITEM | $message"
        )
    }

    // ------------------------------------------------------------
    // LIST EVENTS
    // ------------------------------------------------------------

    fun list(message: String) {

        event(
            "LIST | $message"
        )
    }

    // ------------------------------------------------------------
    // INVITE EVENTS
    // ------------------------------------------------------------

    fun invite(message: String) {

        event(
            "INVITE | $message"
        )
    }

    // ------------------------------------------------------------
    // SYNC EVENTS
    // ------------------------------------------------------------

    fun sync(message: String) {

        event(
            "SYNC | $message"
        )
    }
}