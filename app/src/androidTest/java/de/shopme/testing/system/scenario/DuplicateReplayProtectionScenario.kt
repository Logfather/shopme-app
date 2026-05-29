package de.shopme.testing.system.scenarios

import android.util.Log
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.testing.fake.InMemoryFakeFirestoreGateway
import de.shopme.testing.system.tests.MultiDeviceContextTest
import kotlinx.coroutines.delay
import java.util.UUID

class DuplicateReplayProtectionScenario {

    suspend fun run(
        deviceA: MultiDeviceContextTest,
        deviceB: MultiDeviceContextTest,
        sharedGateway: InMemoryFakeFirestoreGateway
    ) {

        Log.d(
            "HIVRA_TEST",
            "==============================================="
        )

        Log.d(
            "HIVRA_TEST",
            "Starting DuplicateReplayProtectionScenario"
        )

        // ------------------------------------------------
        // STEP 1
        // Create initial item
        // ------------------------------------------------

        val now = System.currentTimeMillis()

        val initialItem =
            ShoppingItemEntity(
                id = UUID.randomUUID().toString(),
                listId = "shared-list",
                name = "Milch",
                quantity = 1,
                category = "Dairy",
                isChecked = false,
                deletedAt = null,
                createdAt = now,
                updatedAt = now
            )

        deviceA.roomRepository
            .createItem(initialItem)

        delay(500)

        Log.d(
            "HIVRA_TEST",
            "Initial item created"
        )

        // ------------------------------------------------
        // STEP 2
        // DeviceB receives initial state
        // ------------------------------------------------

        val remoteInitial =
            sharedGateway.getItem(
                listId = initialItem.listId,
                itemId = initialItem.id
            )
                ?: error("Remote initial item missing")

        deviceB.itemDao.upsert(remoteInitial)

        Log.d(
            "HIVRA_TEST",
            "DeviceB received initial state"
        )

        // ------------------------------------------------
        // STEP 3
        // DeviceA performs UPDATE
        // ------------------------------------------------

        val updated =
            deviceA.itemDao.getById(initialItem.id)
                ?.copy(
                    name = "Hafermilch",
                    quantity = 5,
                    updatedAt = System.currentTimeMillis()
                )
                ?: error("DeviceA item missing")

        deviceA.roomRepository
            .updateItem(updated)

        Log.d(
            "HIVRA_TEST",
            "DeviceA UPDATE queued"
        )

        delay(1000)

        // ------------------------------------------------
        // STEP 4
        // Simulate DUPLICATE replay trigger
        // ------------------------------------------------

        Log.d(
            "HIVRA_TEST",
            "Trigger duplicate replay #1"
        )

        deviceA.syncCoordinator
            .triggerSync()

        delay(300)

        Log.d(
            "HIVRA_TEST",
            "Trigger duplicate replay #2"
        )

        deviceA.syncCoordinator
            .triggerSync()

        delay(300)

        Log.d(
            "HIVRA_TEST",
            "Trigger duplicate replay #3"
        )

        deviceA.syncCoordinator
            .triggerSync()

        delay(2000)

        // ------------------------------------------------
        // STEP 5
        // Pull final remote state
        // ------------------------------------------------

        val finalRemote =
            sharedGateway.getItem(
                listId = initialItem.listId,
                itemId = initialItem.id
            )
                ?: error("Final remote item missing")

        deviceA.itemDao.upsert(finalRemote)
        deviceB.itemDao.upsert(finalRemote)

        // ------------------------------------------------
        // STEP 6
        // Validate convergence
        // ------------------------------------------------

        val finalA =
            deviceA.itemDao.getById(initialItem.id)
                ?: error("DeviceA final item missing")

        val finalB =
            deviceB.itemDao.getById(initialItem.id)
                ?: error("DeviceB final item missing")

        val queueA =
            deviceA.changeQueueDao
                .getPendingChanges()

        val queueB =
            deviceB.changeQueueDao
                .getPendingChanges()

        val consistent =
            finalA.name == "Hafermilch" &&
                    finalB.name == "Hafermilch" &&
                    finalA.quantity == 5 &&
                    finalB.quantity == 5

        Log.d(
            "HIVRA_TEST",
            "FINAL DeviceA name=${finalA.name}"
        )

        Log.d(
            "HIVRA_TEST",
            "FINAL DeviceA quantity=${finalA.quantity}"
        )

        Log.d(
            "HIVRA_TEST",
            "FINAL DeviceB name=${finalB.name}"
        )

        Log.d(
            "HIVRA_TEST",
            "FINAL DeviceB quantity=${finalB.quantity}"
        )

        Log.d(
            "HIVRA_TEST",
            "Remaining queue DeviceA=${queueA.size}"
        )

        Log.d(
            "HIVRA_TEST",
            "Remaining queue DeviceB=${queueB.size}"
        )

        Log.d(
            "HIVRA_TEST",
            "DUPLICATE REPLAY CONSISTENT=$consistent"
        )

        check(consistent) {
            "Duplicate replay corrupted final state"
        }

        check(queueA.isEmpty()) {
            "DeviceA queue not drained"
        }

        check(queueB.isEmpty()) {
            "DeviceB queue not drained"
        }

        Log.d(
            "HIVRA_TEST",
            "Finished DuplicateReplayProtectionScenario"
        )

        Log.d(
            "HIVRA_TEST",
            "==============================================="
        )
    }
}