package de.shopme.data.sync.logging

import android.util.Log
import de.shopme.debug.DebugFlags

object NetworkLog {

    // ------------------------------------------------------------
    // INTERNAL
    // ------------------------------------------------------------

    private fun debug(
        tag: String,
        message: String
    ) {

        if (!DebugFlags.ENABLE_NETWORK_LOGS) {
            return
        }

        Log.d(
            tag,
            message
        )
    }

    private fun error(
        tag: String,
        message: String,
        throwable: Throwable? = null
    ) {

        if (!DebugFlags.ENABLE_NETWORK_LOGS) {
            return
        }

        Log.e(
            tag,
            message,
            throwable
        )
    }

    // ------------------------------------------------------------
    // CONNECTIVITY MONITORING
    // ------------------------------------------------------------

    fun monitor(message: String) {

        debug(
            "NETWORK_MONITOR",
            message
        )
    }

    fun monitorError(
        message: String,
        throwable: Throwable? = null
    ) {

        error(
            "NETWORK_MONITOR",
            message,
            throwable
        )
    }

    // ------------------------------------------------------------
    // NETWORK RECOVERY
    // ------------------------------------------------------------

    fun recovery(message: String) {

        debug(
            "NETWORK_RECOVERY",
            message
        )
    }

    fun recoveryError(
        message: String,
        throwable: Throwable? = null
    ) {

        error(
            "NETWORK_RECOVERY",
            message,
            throwable
        )
    }
}