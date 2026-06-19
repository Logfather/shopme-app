package de.shopme.data.sync.runtime

import de.shopme.data.sync.QueueState

object QueueStateMachine {

    fun canTransition(
        from: QueueState,
        to: QueueState
    ): Boolean {

        return when (from) {

            QueueState.PENDING -> {
                to == QueueState.PROCESSING
            }

            QueueState.PROCESSING -> {
                to == QueueState.DONE ||
                        to == QueueState.RETRY_WAIT ||
                        to == QueueState.FAILED
            }

            QueueState.RETRY_WAIT -> {
                to == QueueState.PROCESSING
            }

            QueueState.DONE -> false

            QueueState.FAILED -> false
        }
    }
}