package de.shopme.data.sync.runtime

enum class SyncRuntimeState {

    IDLE,

    RECOVERING,

    REPLAYING,

    ATTACHING_LISTENERS,

    REALTIME_ACTIVE,

    ERROR
}