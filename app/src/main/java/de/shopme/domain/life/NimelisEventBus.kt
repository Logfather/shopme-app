package de.shopme.domain.life

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

        _events.emit(event)
    }
}