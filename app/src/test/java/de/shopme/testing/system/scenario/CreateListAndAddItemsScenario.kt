package de.shopme.testing.system.scenario

import android.util.Log
import de.shopme.domain.life.LifeEvent
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.testing.system.scenario.HivraSystemScenario
import de.shopme.testing.system.test.HivraSystemTestContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.UUID

class CreateListAndAddItemsScenario : HivraSystemScenario {

    override suspend fun run(
        context: HivraSystemTestContext
    ) {

        Log.d(
            "HIVRA_TEST",
            "CreateListAndAddItemsScenario started"
        )

        // ------------------------------------------------------------
        // EVENT OBSERVER
        // ------------------------------------------------------------

        val observedEvents = mutableListOf<LifeEvent>()

        val eventJob = GlobalScope.launch {

            context.nimelisEventBus.events.collect { event ->

                observedEvents.add(event)

                when (event) {

                    is LifeEvent.ItemAdded -> {

                        Log.d(
                            "HIVRA_TEST_EVENT",
                            "Observed ItemAdded: ${event.itemName}"
                        )
                    }

                    else -> {

                        Log.d(
                            "HIVRA_TEST_EVENT",
                            "Observed event: ${event::class.simpleName}"
                        )
                    }
                }
            }
        }

        // ------------------------------------------------------------
        // CREATE LIST
        // ------------------------------------------------------------

        val listId = UUID.randomUUID().toString()

        val now = System.currentTimeMillis()

        val list = ShoppingListEntity(
            id = listId,
            name = "Edeka",
            ownerId = "test-user",
            storeTypes = emptyList(),
            sharedWith = emptyList(),
            itemCount = 0,
            createdAt = now,
            updatedAt = now,
            deletedAt = null
        )

        context.listDao.insert(list)

        Log.d(
            "HIVRA_TEST",
            "List created: ${list.name}"
        )

        // ------------------------------------------------------------
        // CREATE ITEM 1
        // ------------------------------------------------------------

        val milkItem = ShoppingItemEntity(
            id = UUID.randomUUID().toString(),
            listId = listId,
            name = "Milch",
            quantity = 2,
            category = "Dairy",
            isChecked = false,
            deletedAt = null,
            createdAt = now,
            updatedAt = now
        )

        context.roomRepository.createItem(milkItem)

        Log.d(
            "HIVRA_TEST",
            "Item created: ${milkItem.name}"
        )

        // ------------------------------------------------------------
        // CREATE ITEM 2
        // ------------------------------------------------------------

        val breadItem = ShoppingItemEntity(
            id = UUID.randomUUID().toString(),
            listId = listId,
            name = "Brot",
            quantity = 1,
            category = "Bakery",
            isChecked = false,
            deletedAt = null,
            createdAt = now,
            updatedAt = now
        )

        context.roomRepository.createItem(breadItem)

        Log.d(
            "HIVRA_TEST",
            "Item created: ${breadItem.name}"
        )

        // ------------------------------------------------------------
        // VALIDATE ROOM
        // ------------------------------------------------------------

        val allItems = context.itemDao.getItemsForList(listId)

        Log.d(
            "HIVRA_TEST",
            "Room validation: itemCount=${allItems.size}"
        )

        // ------------------------------------------------------------
        // VALIDATE QUEUE
        // ------------------------------------------------------------

        val queueEntries = context.changeQueueDao.getPendingChanges()

        Log.d(
            "HIVRA_TEST",
            "Queue validation: queueCount=${queueEntries.size}"
        )

        // ------------------------------------------------------------
        // VALIDATE EVENTS
        // ------------------------------------------------------------

        Log.d(
            "HIVRA_TEST",
            "Observed event count=${observedEvents.size}"
        )

        observedEvents.forEach { event ->

            Log.d(
                "HIVRA_TEST",
                "Observed event=${event::class.simpleName}"
            )
        }

        // ------------------------------------------------------------
        // CLEANUP
        // ------------------------------------------------------------

        eventJob.cancel()

        Log.d(
            "HIVRA_TEST",
            "CreateListAndAddItemsScenario finished"
        )
    }
}