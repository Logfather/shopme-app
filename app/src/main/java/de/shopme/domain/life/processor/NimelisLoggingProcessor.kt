package de.shopme.domain.life.processor

import android.util.Log
import de.shopme.domain.life.LifeEvent
import de.shopme.domain.life.NimelisEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NimelisLoggingProcessor(
    private val eventBus: NimelisEventBus,
    private val scope: CoroutineScope
) {

    fun start() {

        scope.launch {

            eventBus.events.collectLatest { event ->

                when (event) {

                    is LifeEvent.ItemAdded -> {
                        Log.d(
                            "NIMELIS_LOGGING",
                            "ItemAdded → item=${event.itemName} list=${event.listId}"
                        )
                    }

                    is LifeEvent.ItemChecked -> {
                        Log.d(
                            "NIMELIS_LOGGING",
                            "ItemChecked → item=${event.itemId} checked=${event.checked}"
                        )
                    }

                    is LifeEvent.ItemDeleted -> {
                        Log.d(
                            "NIMELIS_LOGGING",
                            "ItemDeleted → item=${event.itemId}"
                        )
                    }

                    is LifeEvent.ListCreated -> {
                        Log.d(
                            "NIMELIS_LOGGING",
                            "ListCreated → list=${event.listId}"
                        )
                    }

                    is LifeEvent.ListShared -> {
                        Log.d(
                            "NIMELIS_LOGGING",
                            "ListShared → list=${event.listId}"
                        )
                    }

                    is LifeEvent.InviteAccepted -> {
                        Log.d(
                            "NIMELIS_LOGGING",
                            "InviteAccepted → user=${event.userId}"
                        )
                    }

                    is LifeEvent.SyncConflictDetected -> {
                        Log.d(
                            "NIMELIS_LOGGING",
                            "SyncConflictDetected → entity=${event.entityId}"
                        )
                    }
                }
            }
        }
    }
}