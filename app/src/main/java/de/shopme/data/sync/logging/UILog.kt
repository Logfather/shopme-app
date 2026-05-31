package de.shopme.data.sync.logging

import android.util.Log
import de.shopme.debug.DebugFlags

object UILog {

    // ------------------------------------------------------------
    // INTERNAL
    // ------------------------------------------------------------

    private fun debug(
        tag: String,
        message: String
    ) {

        if (!DebugFlags.ENABLE_UI_LOGS) {
            return
        }

        Log.d(
            tag,
            message
        )
    }

    fun navigation(message: String) {

        debug(
            "UI_NAVIGATION",
            "Actual active Screen = $message"
        )
    }

    fun overlay(message: String) {

        debug(
            "UI_OVERLAY",
            "Actual active Overlay = $message"
        )
    }

//    fun navigationEvent(
//    from: String,
//    to: String
//    )
//
//    fun screenEnter(screen: String)
//
//    fun screenExit(screen: String)
//
//    fun state(message: String)
//
//    fun interaction(message: String)
//
//    fun dialog(message: String)
//
//    fun deeplink(message: String)

}