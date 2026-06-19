package de.shopme.testing.system.tests

import de.shopme.data.sync.QueueState
import de.shopme.data.sync.runtime.QueueStateMachine
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QueueStateMachineTest {

    @Test
    fun pending_canTransitionToProcessing() {

        assertTrue(
            QueueStateMachine.canTransition(
                from = QueueState.PENDING,
                to = QueueState.PROCESSING
            )
        )
    }

    @Test
    fun processing_canTransitionToDone() {

        assertTrue(
            QueueStateMachine.canTransition(
                from = QueueState.PROCESSING,
                to = QueueState.DONE
            )
        )
    }

    @Test
    fun processing_canTransitionToRetryWait() {

        assertTrue(
            QueueStateMachine.canTransition(
                from = QueueState.PROCESSING,
                to = QueueState.RETRY_WAIT
            )
        )
    }

    @Test
    fun processing_canTransitionToFailed() {

        assertTrue(
            QueueStateMachine.canTransition(
                from = QueueState.PROCESSING,
                to = QueueState.FAILED
            )
        )
    }

    @Test
    fun retryWait_canTransitionToProcessing() {

        assertTrue(
            QueueStateMachine.canTransition(
                from = QueueState.RETRY_WAIT,
                to = QueueState.PROCESSING
            )
        )
    }

    @Test
    fun done_cannotTransitionToProcessing() {

        assertFalse(
            QueueStateMachine.canTransition(
                from = QueueState.DONE,
                to = QueueState.PROCESSING
            )
        )
    }

    @Test
    fun failed_cannotTransitionToProcessing() {

        assertFalse(
            QueueStateMachine.canTransition(
                from = QueueState.FAILED,
                to = QueueState.PROCESSING
            )
        )
    }

    @Test
    fun done_cannotTransitionToRetryWait() {

        assertFalse(
            QueueStateMachine.canTransition(
                from = QueueState.DONE,
                to = QueueState.RETRY_WAIT
            )
        )
    }

    @Test
    fun failed_cannotTransitionToRetryWait() {

        assertFalse(
            QueueStateMachine.canTransition(
                from = QueueState.FAILED,
                to = QueueState.RETRY_WAIT
            )
        )
    }
}