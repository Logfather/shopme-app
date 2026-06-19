package de.shopme.testing.system.scenario

import android.util.Log
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.domain.model.StoreType
import de.shopme.testing.system.tests.MultiDeviceContextTest
import java.util.UUID

class MultiDeviceDeleteScenario {


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
            "Starting MultiDeviceDeleteScenario"
        )

        // ============================================================
        // SHARED LIST
        // ============================================================

        val listId = UUID.randomUUID().toString()

        val now = System.currentTimeMillis()

        val sharedList = ShoppingListEntity(
            id = listId,
            name = "Delete Sync List",
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

        // ============================================================
        // CREATE ITEM
        // ============================================================

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

        deviceA.roomRepository.createItem(initialItem)

        kotlinx.coroutines.delay(500)


        Log.d(
            "HIVRA_TEST",
            "DeviceA created item quantity=2"
        )

        // ============================================================
        // REMOTE VALIDATION AFTER CREATE
        // ============================================================

        val remoteAfterCreate =
            deviceA.syncCoordinator
                .firestore
                .getItem(listId, itemId)

        Log.d(
            "HIVRA_TEST",
            "REMOTE after create deletedAt=${remoteAfterCreate?.deletedAt}"
        )

        // ============================================================
        // DEVICE B PULLS REMOTE
        // ============================================================

        if (remoteAfterCreate != null) {

            deviceB.itemDao.upsert(remoteAfterCreate)

            Log.d(
                "HIVRA_TEST",
                "DeviceB pulled remote create state"
            )
        }

        // ============================================================
        // DELETE ON DEVICE A
        // ============================================================

        val currentOnA =
            deviceA.itemDao.getById(itemId)
                ?: throw IllegalStateException(
                    "DeviceA item missing before delete"
                )

        deviceA.roomRepository.deleteItem(currentOnA)

        Log.d(
            "HIVRA_TEST",
            "BEFORE DELAY"
        )

        deviceA.syncCoordinator.awaitIdle()

        Log.d(
            "HIVRA_TEST",
            "AFTER DELAY"
        )

        Log.d(
            "HIVRA_TEST",
            "DeviceA deleted item"
        )

        // ============================================================
        // REMOTE VALIDATION AFTER DELETE
        // ============================================================

        val remoteAfterDelete =
            deviceA.syncCoordinator
                .firestore
                .getItem(listId, itemId)

        Log.d(
            "HIVRA_TEST",
            "REMOTE after delete deletedAt=${remoteAfterDelete?.deletedAt}"
        )

        // ============================================================
        // DEVICE B PULLS REMOTE DELETE
        // ============================================================

        if (remoteAfterDelete != null) {

            deviceB.itemDao.upsert(remoteAfterDelete)

            Log.d(
                "HIVRA_TEST",
                "DeviceB pulled remote delete state"
            )
        }

        // ============================================================
        // FINAL VALIDATION
        // ============================================================

        val finalA =
            deviceA.itemDao.getById(itemId)

        val finalB =
            deviceB.itemDao.getById(itemId)

        val finalRemote =
            deviceA.syncCoordinator
                .firestore
                .getItem(listId, itemId)

        Log.d(
            "HIVRA_TEST",
            "FINAL DeviceA deletedAt=${finalA?.deletedAt}"
        )

        Log.d(
            "HIVRA_TEST",
            "FINAL DeviceB deletedAt=${finalB?.deletedAt}"
        )

        Log.d(
            "HIVRA_TEST",
            "FINAL Remote deletedAt=${finalRemote?.deletedAt}"
        )

        val deleteConsistent =
            finalA?.deletedAt != null &&
                    finalB?.deletedAt != null &&
                    finalRemote?.deletedAt != null

        Log.d(
            "HIVRA_TEST",
            "DELETE CONSISTENT=$deleteConsistent"
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
            "Finished MultiDeviceDeleteScenario"
        )

        Log.d(
            "HIVRA_TEST",
            "==============================================="
        )
    }
}