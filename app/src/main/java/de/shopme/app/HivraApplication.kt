package de.shopme.app

import android.app.Application
import android.os.Process
import de.shopme.data.sync.logging.RuntimeLog
import de.shopme.runtime.HivraRuntime

class HivraApplication : Application() {

    lateinit var runtime: HivraRuntime
        private set

    override fun onCreate() {
        super.onCreate()

        RuntimeLog.appStartError(
            "HivraApplication started | pid=${Process.myPid()}"
        )

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->

            RuntimeLog.fatal(
                "Uncaught exception in thread=${thread.name}",
                throwable
            )
        }

        RuntimeLog.appStart(
            "Global crash handler installed"
        )

        // ------------------------------------------------------------
        // HIVRA RUNTIME
        // ------------------------------------------------------------

        runtime = HivraRuntime(this)

        runtime.start()

        RuntimeLog.appStart(
            "HivraRuntime started"
        )
    }
}