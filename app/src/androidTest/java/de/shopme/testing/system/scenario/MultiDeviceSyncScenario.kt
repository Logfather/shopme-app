package de.shopme.testing.system.scenario

import android.util.Log
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.domain.model.StoreType
import de.shopme.testing.system.tests.MultiDeviceContextTest
import kotlinx.coroutines.delay
import java.util.UUID

class MultiDeviceSyncScenario {

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
            "Starting MultiDeviceSyncScenario"
        )

        // ============================================================
        // SHARED INITIAL STATE
        // ============================================================

        val listId = UUID.randomUUID().toString()

        val now = System.currentTimeMillis()

        val list = ShoppingListEntity(
            id = listId,
            name = "Shared Sync List",
            ownerId = "test-user",
            storeTypes = listOf(StoreType.EDEKA),
            sharedWith = emptyList(),
            itemCount = 0,
            createdAt = now,
            updatedAt = now,
            deletedAt = null
        )

        deviceA.listDao.insert(list)
        deviceB.listDao.insert(list)

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
        // DEVICE A CREATES ITEM
        // ============================================================

        deviceA.roomRepository.createItem(initialItem)
        deviceA.syncCoordinator.awaitIdle()
        Log.d(
            "HIVRA_TEST",
            "DeviceA created item quantity=2"
        )

        // ============================================================
        // WAIT FOR SYNC TO REMOTE
        // ============================================================

        delay(1000)

        val remoteBeforeUpdate =
            deviceA.syncCoordinator
                .firestore
                .getItem(listId, itemId)

        Log.d(
            "HIVRA_TEST",
            "REMOTE after create quantity=${remoteBeforeUpdate?.quantity}"
        )

        // ============================================================
        // DEVICE A UPDATE
        // ============================================================

        val updatedItem = initialItem.copy(
            quantity = 5,
            updatedAt = System.currentTimeMillis()
        )

        deviceA.roomRepository.updateItem(updatedItem)

        deviceA.syncCoordinator.awaitIdle()

        Log.d(
            "HIVRA_TEST",
            "DeviceA updated quantity=5"
        )

        // ============================================================
        // WAIT FOR REMOTE SYNC
        // ============================================================

        delay(1500)

        val remoteAfterUpdate =
            deviceA.syncCoordinator
                .firestore
                .getItem(listId, itemId)

        Log.d(
            "HIVRA_TEST",
            "REMOTE after update quantity=${remoteAfterUpdate?.quantity}"
        )

        // ============================================================
        // DEVICE B PULLS REMOTE STATE
        // ============================================================

        if (remoteAfterUpdate != null) {

            deviceB.itemDao.upsert(remoteAfterUpdate)

            Log.d(
                "HIVRA_TEST",
                "DeviceB pulled remote state"
            )
        }

        // ============================================================
        // FINAL VALIDATION
        // ============================================================

        val finalA = deviceA.itemDao.getById(itemId)

        val finalB = deviceB.itemDao.getById(itemId)

        Log.d(
            "HIVRA_TEST",
            "FINAL DeviceA quantity=${finalA?.quantity}"
        )

        Log.d(
            "HIVRA_TEST",
            "FINAL DeviceB quantity=${finalB?.quantity}"
        )

        Log.d(
            "HIVRA_TEST",
            "FINAL Remote quantity=${remoteAfterUpdate?.quantity}"
        )

        val allEqual =
            finalA?.quantity ==
                    finalB?.quantity &&
                    finalB?.quantity ==
                    remoteAfterUpdate?.quantity

        Log.d(
            "HIVRA_TEST",
            "SYNC CONSISTENT=$allEqual"
        )

        // ============================================================
        // QUEUE VALIDATION
        // ============================================================

        val queueA =
            deviceA.changeQueueDao.getAllChanges()

        val queueB =
            deviceB.changeQueueDao.getAllChanges()

        Log.d(
            "HIVRA_TEST",
            "DeviceA queue count=${queueA.size}"
        )

        queueA.forEach {

            Log.d(
                "HIVRA_TEST",
                "DeviceA queue op=${it.operation} state=${it.state}"
            )
        }

        Log.d(
            "HIVRA_TEST",
            "DeviceB queue count=${queueB.size}"
        )

        queueB.forEach {

            Log.d(
                "HIVRA_TEST",
                "DeviceB queue op=${it.operation} state=${it.state}"
            )
        }

        Log.d(
            "HIVRA_TEST",
            "Finished MultiDeviceSyncScenario"
        )

        Log.d(
            "HIVRA_TEST",
            "==============================================="
        )
    }
}