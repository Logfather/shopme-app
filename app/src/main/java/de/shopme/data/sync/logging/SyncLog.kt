package de.shopme.data.sync.logging

import android.util.Log
import de.shopme.debug.DebugFlags

object SyncLog {

    // ------------------------------------------------------------
    // INTERNAL
    // ------------------------------------------------------------

    private fun debug(
        tag: String,
        message: String
    ) {

        if (!DebugFlags.ENABLE_SYNC_LOGS) {
            return
        }

        Log.d(
            tag,
            message
        )
    }

    // ------------------------------------------------------------
    // REPLAY / ORCHESTRATION
    // ------------------------------------------------------------

    fun orchestrator(message: String) {

        debug(
            "SYNC_ORCHESTRATOR",
            message
        )
    }

    fun scheduler(message: String) {

        debug(
            "SYNC_SCHEDULER",
            message
        )
    }

    fun worker(message: String) {

        debug(
            "SYNC_WORKER",
            message
        )
    }

    // ------------------------------------------------------------
    // QUEUE RUNTIME
    // ------------------------------------------------------------

    fun queue(message: String) {

        debug(
            "SYNC_QUEUE",
            message
        )
    }

    // ------------------------------------------------------------
    // APPLY / RECONCILIATION
    // ------------------------------------------------------------

    fun apply(message: String) {

        debug(
            "SYNC_APPLY",
            message
        )
    }

    // ------------------------------------------------------------
    // CONFLICT RESOLUTION
    // ------------------------------------------------------------

    fun conflict(message: String) {

        debug(
            "SYNC_CONFLICT",
            message
        )
    }

    // ------------------------------------------------------------
    // RECOVERY / RETRY
    // ------------------------------------------------------------

    fun recovery(
        message: String,
        throwable: Throwable? = null
    ) {

        if (throwable != null) {

            Log.e(
                "SYNC_RECOVERY",
                message,
                throwable
            )

        } else {

            debug(
                "SYNC_RECOVERY",
                message
            )
        }
    }

    // ------------------------------------------------------------
    // REALTIME SYNC
    // ------------------------------------------------------------

    fun realtime(message: String) {

        debug(
            "SYNC_REALTIME",
            message
        )
    }

    // ------------------------------------------------------------
    // GUARDS / SUPPRESSION
    // ------------------------------------------------------------

    fun guard(message: String) {

        debug(
            "SYNC_GUARD",
            message
        )
    }

    // ------------------------------------------------------------
    // LIFECYCLE
    // ------------------------------------------------------------

    fun lifecycle(message: String) {

        debug(
            "SYNC_LIFECYCLE",
            message
        )
    }

}