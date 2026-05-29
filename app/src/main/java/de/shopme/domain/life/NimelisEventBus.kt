package de.shopme.domain.life

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class NimelisEventBus {

    private val _events = MutableSharedFlow<LifeEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )

    val events: SharedFlow<LifeEvent> = _events.asSharedFlow()

    suspend fun emit(event: LifeEvent) {

        Log.d(
            "NIMELIS_EVENT_BUS",
            "Emit event: ${event::class.simpleName}"
        )

        _events.emit(event)
    }
}