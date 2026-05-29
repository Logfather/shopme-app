package de.shopme.testing.system.scenario

import android.util.Log
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.domain.model.StoreType
import de.shopme.testing.system.tests.MultiDeviceContextTest
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.util.UUID

class MultiDeviceConflictScenario {

    suspend fun run(
        deviceA: MultiDeviceContextTest,
        deviceB: MultiDeviceContextTest
    ) {

        Log.d(
            "HIVRA_TEST",
            "==============================================="
        )

        Log.d(
            "HIVRA_TEST",
            "Starting MultiDeviceConflictScenario"
        )

        // ============================================================
        // SHARED REMOTE STATE INIT
        // ============================================================

        val listId = UUID.randomUUID().toString()

        val now = System.currentTimeMillis()

        val sharedList = ShoppingListEntity(
            id = listId,
            name = "Multi Device List",
            ownerId = "test-user",
            storeTypes = listOf(StoreType.EDEKA),
            sharedWith = emptyList(),
            itemCount = 0,
            createdAt = now,
            updatedAt = now,
            deletedAt = null
        )

        deviceA.listDao.insert(sharedList)
        deviceB.listDao.insert(sharedList)

        val itemId = UUID.randomUUID().toString()

        val initialItem = ShoppingItemEntity(
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

        // ============================================================
        // BOTH DEVICES START WITH SAME STATE
        // ============================================================


        deviceA.roomRepository.createItem(initialItem)
        deviceB.roomRepository.createItem(initialItem)

        Log.d(
            "HIVRA_TEST",
            "Initial shared state created quantity=2"
        )

        // ============================================================
        // DEVICE CONFLICT
        // ============================================================

        coroutineScope {

            val deviceAJob = async {

                val updated = initialItem.copy(
                    quantity = 5,
                    updatedAt = System.currentTimeMillis()
                )

                deviceA.roomRepository.updateItem(updated)

                Log.d(
                    "HIVRA_TEST",
                    "DeviceA updated quantity=5"
                )
            }

            val deviceBJob = async {

                val updated = initialItem.copy(
                    quantity = 9,
                    updatedAt = System.currentTimeMillis()
                )

                deviceB.roomRepository.updateItem(updated)

                Log.d(
                    "HIVRA_TEST",
                    "DeviceB updated quantity=9"
                )
            }

            awaitAll(
                deviceAJob,
                deviceBJob
            )

            // ============================================================
            // TRIGGER REMOTE SYNC
            // ============================================================
            deviceA.syncCoordinator.triggerSync()
            deviceB.syncCoordinator.triggerSync()

            delay(2000)
        }

        // ============================================================
        // FINAL DEVICE STATES
        // ============================================================

        val finalA = deviceA.itemDao.getById(itemId)

        val finalB = deviceB.itemDao.getById(itemId)

        Log.d(
            "HIVRA_TEST",
            "FINAL DeviceA quantity=${finalA?.quantity} updatedAt=${finalA?.updatedAt}"
        )

        Log.d(
            "HIVRA_TEST",
            "FINAL DeviceB quantity=${finalB?.quantity} updatedAt=${finalB?.updatedAt}"
        )

        // ============================================================
        // QUEUE VALIDATION
        // ============================================================

        val queueA = deviceA.changeQueueDao.getAllChanges()

        val queueB = deviceB.changeQueueDao.getAllChanges()

        Log.d(
            "HIVRA_TEST",
            "DeviceA queue count=${queueA.size}"
        )

        queueA.forEach {

            Log.d(
                "HIVRA_TEST",
                "DeviceA queue op=${it.operation} entity=${it.entityId}"
            )
        }

        Log.d(
            "HIVRA_TEST",
            "DeviceB queue count=${queueB.size}"
        )

        queueB.forEach {

            Log.d(
                "HIVRA_TEST",
                "DeviceB queue op=${it.operation} entity=${it.entityId}"
            )
        }

        // ============================================================
        // CONFLICT VALIDATION
        // ============================================================

        val sameState =
            finalA?.quantity == finalB?.quantity

        Log.d(
            "HIVRA_TEST",
            "Conflict resolved consistently=$sameState"
        )

        Log.d(
            "HIVRA_TEST",
            "Finished MultiDeviceConflictScenario"
        )

        Log.d(
            "HIVRA_TEST",
            "==============================================="
        )
    }
}