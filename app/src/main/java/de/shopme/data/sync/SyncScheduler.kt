package de.shopme.data.sync

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import de.shopme.data.sync.model.ReplayRequest
import java.util.concurrent.TimeUnit

class SyncScheduler(
    private val context: Context
) {

    companion object {

        private const val UNIQUE_SYNC_WORK =
            "shopme_sync"
    }

    fun enqueueReplay(
        request: ReplayRequest
    ) {

        Log.d(
            "SYNC_SCHEDULER",
            "Replay enqueued | id=${request.replayId} | reason=${request.reason}"
        )

        val constraints =
            Constraints.Builder()
                .setRequiredNetworkType(
                    NetworkType.CONNECTED
                )
                .build()

        val workRequest =
            OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    10,
                    TimeUnit.SECONDS
                )
                .setInputData(
                    workDataOf(
                        "replay_id" to request.replayId,
                        "replay_reason" to request.reason.name
                    )
                )
                .build()

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(
                UNIQUE_SYNC_WORK,
                ExistingWorkPolicy.KEEP,
                workRequest
            )
    }
}