package de.shopme.data.sync.logging

import android.util.Log
import de.shopme.debug.DebugFlags

object RuntimeLog {

    // ------------------------------------------------------------
    // INTERNAL
    // ------------------------------------------------------------


    private fun safeDebug(
        tag: String,
        message: String
    ) {
        try {

            android.util.Log.d(
                tag,
                message
            )

        } catch (_: RuntimeException) {

            println("$tag: $message")
        }
    }

    private fun debug(
        tag: String,
        message: String
    ) {

        if (!DebugFlags.ENABLE_RUNTIME_LOGS) {
            return
        }

        safeDebug(
            tag = tag,
            message = message
        )
    }

    private fun error(
        tag: String,
        message: String,
        throwable: Throwable? = null
    ) {

        if (!DebugFlags.ENABLE_RUNTIME_LOGS) {
            return
        }

        Log.e(
            tag,
            message,
            throwable
        )
    }

    fun fatal(
        message: String,
        throwable: Throwable? = null
    ) {

        error(
            "FATAL_CRASH",
            message,
            throwable
        )
    }

    // ------------------------------------------------------------
    // APPLICATION STARTUP
    // ------------------------------------------------------------

    fun appStart(message: String) {

        debug(
            "APP_START",
            message
        )
    }

    fun appStartError(
        message: String,
        throwable: Throwable? = null
    ) {

        error(
            "APP_START",
            message,
            throwable
        )
    }

    // ------------------------------------------------------------
    // RUNTIME LIFECYCLE
    // ------------------------------------------------------------

    fun runtime(message: String) {

        debug(
            "HIVRA_RUNTIME",
            message
        )
    }

    fun runtimeError(
        message: String,
        throwable: Throwable? = null
    ) {

        error(
            "HIVRA_RUNTIME",
            message,
            throwable
        )
    }

    // ------------------------------------------------------------
    // RUNTIME RECOVERY
    // ------------------------------------------------------------

    fun recovery(message: String) {

        debug(
            "RUNTIME_RECOVERY",
            message
        )
    }

    fun recoveryError(
        message: String,
        throwable: Throwable? = null
    ) {

        error(
            "RUNTIME_RECOVERY",
            message,
            throwable
        )
    }

    // ------------------------------------------------------------
    // AUTH
    // ------------------------------------------------------------

    fun auth(message: String) {

        debug(
            "RUNTIME_AUTH",
            message
        )
    }

    fun account(message: String) {

        debug(
            "RUNTIME_ACCOUNT",
            message
        )
    }

    // ------------------------------------------------------------
    // PROFILE
    // ------------------------------------------------------------

    fun profile(message: String) {

        debug(
            "PROFILE",
            message
        )
    }

    // ------------------------------------------------------------
    // SYNC
    // ------------------------------------------------------------

    fun sync(message: String) {

        debug(
            "SYNC",
            message
        )
    }

    // ------------------------------------------------------------
    // EFFECT
    // ------------------------------------------------------------

    fun effect(message: String) {

        debug(
            "EFFECT",
            message
        )
    }

    // ------------------------------------------------------------
    // TRACE CREATION
    // ------------------------------------------------------------

    fun creation(message: String) {

        debug(
            "CREATE_TRACE",
            message
        )
    }

    // ------------------------------------------------------------
    // REDUCER
    // ------------------------------------------------------------

    fun reducer(message: String) {

        debug(
            "REDUCER",
            message
        )
    }

    // ------------------------------------------------------------
    // LIST
    // ------------------------------------------------------------

    fun list(message: String) {

        debug(
            "LIST",
            message
        )
    }

    // ------------------------------------------------------------
    // SHARE
    // ------------------------------------------------------------

    fun share(message: String) {

        debug(
            "SHARE",
            message
        )
    }

    // ------------------------------------------------------------
    // INVITE
    // ------------------------------------------------------------

    fun invite(message: String) {

        debug(
            "INVITE",
            message
        )
    }

    fun speech(
        message: String,
        throwable: Throwable? = null
    ) {

        if (throwable != null) {

            Log.e(
                "SHOPME_SPEECH",
                message,
                throwable
            )

        } else {

            Log.d(
                "SHOPME_SPEECH",
                message
            )
        }
    }



    // ------------------------------------------------------------
    // QUEUE
    // ------------------------------------------------------------


    fun queue(
        message: String,
        throwable: Throwable? = null
    ) {

        if (throwable != null) {

            Log.e(
                "QUEUE",
                message,
                throwable
            )

        } else {

            Log.d(
                "QUEUE",
                message
            )
        }
    }
}