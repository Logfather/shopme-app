package de.shopme.data.sync.model

enum class ReplayExecutionState {

    IDLE,

    RUNNING,

    FAILED,

    INTERRUPTED,

    RETRYING,

    RECOVERING
}