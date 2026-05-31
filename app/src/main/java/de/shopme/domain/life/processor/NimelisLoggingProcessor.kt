package de.shopme.domain.life.processor

import de.shopme.data.sync.logging.LifeLog
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
                        LifeLog.item(
                            "Added | item=${event.itemName} | list=${event.listId}"
                        )
                    }

                    is LifeEvent.ItemChecked -> {
                        LifeLog.item(
                            "Checked | item=${event.itemId} | checked=${event.checked}"
                        )
                    }

                    is LifeEvent.ItemDeleted -> {
                        LifeLog.item(
                            "Deleted | item=${event.itemId}"
                        )
                    }

                    is LifeEvent.ListCreated -> {
                        LifeLog.list(
                            "Created | list=${event.listId}"
                        )
                    }

                    is LifeEvent.ListShared -> {
                        LifeLog.list(
                            "Shared | list=${event.listId}"
                        )
                    }

                    is LifeEvent.InviteAccepted -> {
                        LifeLog.invite(
                            "Accepted | user=${event.userId}"
                        )
                    }

                    is LifeEvent.SyncConflictDetected -> {
                        LifeLog.sync(
                            "Conflict detected | entity=${event.entityId}"
                        )
                    }
                }
            }
        }
    }
}