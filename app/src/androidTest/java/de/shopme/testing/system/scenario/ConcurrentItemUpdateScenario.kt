package de.shopme.testing.system.scenario

import android.util.Log
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.domain.model.StoreType
import de.shopme.testing.system.tests.HivraSystemContextTest
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.UUID

class ConcurrentItemUpdateScenario : HivraSystemScenario {

    override suspend fun run(
        context: HivraSystemContextTest
    ) {

        Log.d(
            "HIVRA_TEST",
            "ConcurrentItemUpdateScenario started"
        )

        val listId = UUID.randomUUID().toString()

        val list = ShoppingListEntity(
            id = listId,
            name = "Concurrent Test",
            ownerId = "test-user",
            storeTypes = listOf(StoreType.EDEKA),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        context.listDao.upsert(list)

        val itemId = UUID.randomUUID().toString()

        val initialItem = ShoppingItemEntity(
            id = itemId,
            listId = listId,
            name = "Milch",
            quantity = 2,
            category = "Food",
            isChecked = false,
            deletedAt = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        context.roomRepository.createItem(initialItem)

        Log.d(
            "HIVRA_TEST",
            "Initial item created quantity=2"
        )

        coroutineScope {

            val deviceA = async {

                repeat(5) { index ->

                    val updated = initialItem.copy(
                        quantity = 5 + index,
                        updatedAt = System.currentTimeMillis()
                    )

                    context.roomRepository.updateItem(updated)

                    Log.d(
                        "HIVRA_TEST",
                        "DeviceA update quantity=${updated.quantity}"
                    )
                }
            }

            val deviceB = async {

                repeat(5) { index ->

                    val updated = initialItem.copy(
                        quantity = 9 + index,
                        updatedAt = System.currentTimeMillis()
                    )

                    context.roomRepository.updateItem(updated)

                    Log.d(
                        "HIVRA_TEST",
                        "DeviceB update quantity=${updated.quantity}"
                    )
                }
            }

            awaitAll(deviceA, deviceB)
        }

        val finalItem = context.itemDao.getById(itemId)

        Log.d(
            "HIVRA_TEST",
            "FINAL ITEM quantity=${finalItem?.quantity} updatedAt=${finalItem?.updatedAt}"
        )

        val queueEntries = context.changeQueueDao.getAllChanges()

        Log.d(
            "HIVRA_TEST",
            "FINAL QUEUE COUNT=${queueEntries.size}"
        )

        queueEntries.forEach {

            Log.d(
                "HIVRA_TEST",
                "QUEUE ENTRY op=${it.operation} entity=${it.entityId}"
            )
        }

        Log.d(
            "HIVRA_TEST",
            "ConcurrentItemUpdateScenario finished"
        )
    }
}