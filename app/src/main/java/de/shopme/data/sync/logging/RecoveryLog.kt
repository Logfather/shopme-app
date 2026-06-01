package de.shopme.data.sync.logging

import android.util.Log
import de.shopme.debug.DebugFlags

object RecoveryLog {

    // ------------------------------------------------------------
    // INTERNAL
    // ------------------------------------------------------------

    private fun debug(
        tag: String,
        message: String
    ) {

        if (!DebugFlags.ENABLE_RECOVERY_LOGS) {
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

        if (!DebugFlags.ENABLE_RECOVERY_LOGS) {
            return
        }

        Log.e(
            tag,
            message,
            throwable
        )
    }

    // ------------------------------------------------------------
    // REPLAY RECOVERY
    // ------------------------------------------------------------

    fun replay(message: String) {

        debug(
            "RECOVERY_REPLAY",
            message
        )
    }

    fun replayError(
        message: String,
        throwable: Throwable? = null
    ) {

        error(
            "RECOVERY_REPLAY",
            message,
            throwable
        )
    }

    // ------------------------------------------------------------
    // PROCESS RECOVERY
    // ------------------------------------------------------------

    fun process(message: String) {

        debug(
            "RECOVERY_PROCESS",
            message
        )
    }

    fun processError(
        message: String,
        throwable: Throwable? = null
    ) {

        error(
            "RECOVERY_PROCESS",
            message,
            throwable
        )
    }

    // ------------------------------------------------------------
    // RETRY / RECOVERY POLICY
    // ------------------------------------------------------------

    fun policy(message: String) {

        debug(
            "RECOVERY_POLICY",
            message
        )
    }

    fun policyError(
        message: String,
        throwable: Throwable? = null
    ) {

        error(
            "RECOVERY_POLICY",
            message,
            throwable
        )
    }

    fun sync(
        message: String,
        throwable: Throwable? = null
    ) {
        if (throwable != null) {
            Log.e("SHOPME_SYNC", message, throwable)
        } else {
            Log.d("SHOPME_SYNC", message)
        }
    }
}