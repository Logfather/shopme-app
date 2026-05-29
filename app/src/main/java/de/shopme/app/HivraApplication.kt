package de.shopme.app

import android.app.Application
import android.os.Process
import android.util.Log
import de.shopme.runtime.HivraRuntime

class HivraApplication : Application() {

    lateinit var runtime: HivraRuntime
        private set

    override fun onCreate() {
        super.onCreate()

        Log.e(
            "APP_START",
            "HivraApplication started | pid=${Process.myPid()}"
        )

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->

            Log.e(
                "FATAL_CRASH",
                "Uncaught exception in thread=${thread.name}",
                throwable
            )
        }

        Log.d(
            "APP_START",
            "Global crash handler installed"
        )

        // ------------------------------------------------------------
        // HIVRA RUNTIME
        // ------------------------------------------------------------

        runtime = HivraRuntime(this)

        runtime.start()

        Log.d(
            "APP_START",
            "HivraRuntime started"
        )
    }
}