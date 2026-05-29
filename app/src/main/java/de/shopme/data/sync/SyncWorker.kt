package de.shopme.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.shopme.app.HivraApplication

class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {

        Log.d(
            "SYNC_WORKER",
            "Worker started"
        )

        return try {

            val runtime =
                (applicationContext as HivraApplication).runtime

            runtime.syncCoordinator.triggerSync()

            Log.d(
                "SYNC_WORKER",
                "Sync triggered successfully"
            )

            Result.success()

        } catch (t: Throwable) {

            Log.e(
                "SYNC_WORKER",
                "Worker failed",
                t
            )

            Result.retry()
        }
    }
}