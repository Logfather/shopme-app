package de.shopme.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.shopme.app.HivraApplication
import de.shopme.data.sync.logging.RecoveryLog
import de.shopme.data.sync.logging.RuntimeLog

class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {

        val replayId =
            inputData.getString("replay_id")

        val replayReason =
            inputData.getString("replay_reason")


        RuntimeLog.sync(
            "Worker started | replayId=$replayId | reason=$replayReason"
        )

        return try {

            val runtime =
                (applicationContext as HivraApplication).runtime

            runtime.syncCoordinator.triggerSync()

            RuntimeLog.sync(
                "Sync success | replayId=$replayId"
            )

            runtime
                .replayCompletionNotifier
                .onReplayCompleted()

            Result.success()

        } catch (e: Exception) {

            RecoveryLog.sync(
                "Sync failed | replayId=$replayId",
                e
            )

            Result.retry()
        }
    }
}