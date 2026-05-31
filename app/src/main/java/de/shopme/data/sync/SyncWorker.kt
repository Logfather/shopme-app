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

        val replayId =
            inputData.getString("replay_id")

        val replayReason =
            inputData.getString("replay_reason")


        Log.d(
            "SYNC_WORKER",
            "Worker started | replayId=$replayId | reason=$replayReason"
        )

        return try {

            val runtime =
                (applicationContext as HivraApplication).runtime

            runtime.syncCoordinator.triggerSync()

            Log.d(
                "SYNC_WORKER",
                "Sync success | replayId=$replayId"
            )

            runtime
                .replayCompletionNotifier
                .onReplayCompleted()

            Result.success()

        } catch (e: Exception) {

            Log.e(
                "SYNC_WORKER",
                "Sync failed | replayId=$replayId",
                e
            )

            Result.retry()
        }
    }
}