package de.shopme.testing.system.scenario

import android.util.Log
import de.shopme.domain.life.LifeEvent
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.testing.system.tests.HivraSystemContextTest
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.util.UUID

class UpdateExistingItemScenario : HivraSystemScenario {

    override suspend fun run(
        context: HivraSystemContextTest
    ) {

        Log.d(
            "HIVRA_TEST",
            "UpdateExistingItemScenario started"
        )

        // ============================================================
        // EVENT OBSERVER
        // ============================================================

        val observedEvents = mutableListOf<LifeEvent>()

        val eventJob = GlobalScope.launch {

            context.nimelisEventBus.events
                .take(4)
                .collect { event ->

                    observedEvents.add(event)

                    Log.d(
                        "HIVRA_TEST_EVENT",
                        "Observed event=${event::class.simpleName}"
                    )
                }
        }

        // ============================================================
        // CREATE LIST
        // ============================================================

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
            "List created"
        )

        // ============================================================
        // CREATE ITEM
        // ============================================================

        val itemId = UUID.randomUUID().toString()

        val milk = ShoppingItemEntity(
            id = itemId,
            listId = listId,
            name = "Milch",
            quantity = 2,
            category = "Dairy",
            isChecked = false,
            deletedAt = null,
            createdAt = now,
            updatedAt = now
        )

        context.roomRepository.createItem(milk)

        Log.d(
            "HIVRA_TEST",
            "CREATE executed quantity=2"
        )

        // ============================================================
        // UPDATE #1
        // ============================================================

        val updatedMilk1 = milk.copy(
            quantity = 5,
            updatedAt = System.currentTimeMillis()
        )

        context.roomRepository.updateItem(updatedMilk1)

        Log.d(
            "HIVRA_TEST",
            "UPDATE #1 executed quantity=5"
        )

        // ============================================================
        // UPDATE #2
        // ============================================================

        val updatedMilk2 = updatedMilk1.copy(
            quantity = 8,
            updatedAt = System.currentTimeMillis()
        )

        context.roomRepository.updateItem(updatedMilk2)

        Log.d(
            "HIVRA_TEST",
            "UPDATE #2 executed quantity=8"
        )

        // ============================================================
        // DELETE
        // ============================================================

        context.roomRepository.deleteItem(updatedMilk2)

        Log.d(
            "HIVRA_TEST",
            "DELETE executed"
        )

        // ============================================================
        // ROOM VALIDATION
        // ============================================================

        val items = context.itemDao.getItemsForList(listId)

        Log.d(
            "HIVRA_TEST",
            "Room item count=${items.size}"
        )

        items.forEach {

            Log.d(
                "HIVRA_TEST",
                "Room item name=${it.name} quantity=${it.quantity} deletedAt=${it.deletedAt}"
            )
        }

        // ============================================================
        // QUEUE VALIDATION
        // ============================================================

        val allChanges = context.changeQueueDao.getAllChanges()

        Log.d(
            "HIVRA_TEST",
            "Queue total count=${allChanges.size}"
        )

        allChanges.forEach {

            Log.d(
                "HIVRA_TEST",
                "QueueEntry op=${it.operation} state=${it.state} entity=${it.entityId}"
            )
        }

        // ============================================================
        // EVENT VALIDATION
        // ============================================================

        Log.d(
            "HIVRA_TEST",
            "Observed event count=${observedEvents.size}"
        )

        observedEvents.forEach {

            Log.d(
                "HIVRA_TEST",
                "Observed event=${it::class.simpleName}"
            )
        }

        // ============================================================
        // CLEANUP
        // ============================================================

        eventJob.cancel()

        Log.d(
            "HIVRA_TEST",
            "UpdateExistingItemScenario finished"
        )
    }
}