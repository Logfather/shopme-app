package de.shopme.testing.system.scenarios

import android.util.Log
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.testing.fake.InMemoryFakeFirestoreGateway
import de.shopme.testing.system.tests.MultiDeviceContextTest
import kotlinx.coroutines.delay
import java.util.UUID

class OfflineReplayScenario {

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
            "Starting OfflineReplayScenario"
        )

        // ------------------------------------------------
        // STEP 1
        // DeviceA creates initial item online
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
            "Initial item created online"
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
        // Simulate OFFLINE mode
        // ------------------------------------------------

        sharedGateway.isNetworkAvailable = false

        Log.d(
            "HIVRA_TEST",
            "Simulated OFFLINE mode"
        )

        // ------------------------------------------------
        // STEP 4
        // DeviceA performs multiple offline updates
        // ------------------------------------------------

        val item1 =
            deviceA.itemDao.getById(initialItem.id)
                ?: error("Item missing on DeviceA")

        deviceA.roomRepository.updateItem(
            item1.copy(
                name = "Hafermilch"
            )
        )

        Log.d(
            "HIVRA_TEST",
            "Offline update #1 -> Hafermilch"
        )

        delay(50)

        val item2 =
            deviceA.itemDao.getById(initialItem.id)
                ?: error("Item missing after update #1")

        deviceA.roomRepository.updateItem(
            item2.copy(
                quantity = 3
            )
        )

        Log.d(
            "HIVRA_TEST",
            "Offline update #2 -> quantity=3"
        )

        delay(50)

        val item3 =
            deviceA.itemDao.getById(initialItem.id)
                ?: error("Item missing after update #2")

        deviceA.roomRepository.updateItem(
            item3.copy(
                isChecked = true
            )
        )

        Log.d(
            "HIVRA_TEST",
            "Offline update #3 -> checked=true"
        )

        delay(500)

        // ------------------------------------------------
        // STEP 5
        // Validate queue accumulated offline changes
        // ------------------------------------------------

        val pendingBeforeReconnect =
            deviceA.changeQueueDao.getPendingChanges()

        Log.d(
            "HIVRA_TEST",
            "Pending queue before reconnect size=${pendingBeforeReconnect.size}"
        )

        check(pendingBeforeReconnect.isNotEmpty()) {
            "Expected pending offline queue"
        }

        // ------------------------------------------------
        // STEP 6
        // Simulate reconnect
        // ------------------------------------------------

        sharedGateway.isNetworkAvailable = true

        deviceA.changeQueueDao
            .resetRetryBackoff()

        Log.d(
            "HIVRA_TEST",
            "Simulated RECONNECT"
        )

        // ------------------------------------------------
        // STEP 7
        // Trigger replay sync
        // ------------------------------------------------

        deviceA.syncCoordinator.triggerSync()

        delay(1500)

        // ------------------------------------------------
        // STEP 8
        // Pull final remote state
        // ------------------------------------------------

        val finalRemote =
            sharedGateway.getItem(
                listId = initialItem.listId,
                itemId = initialItem.id
            )
                ?: error("Final remote item missing")

        deviceB.itemDao.upsert(finalRemote)

        // ------------------------------------------------
        // STEP 9
        // Validate replay convergence
        // ------------------------------------------------

        val finalA =
            deviceA.itemDao.getById(initialItem.id)
                ?: error("DeviceA final missing")

        val finalB =
            deviceB.itemDao.getById(initialItem.id)
                ?: error("DeviceB final missing")

        val queueAfterReplay =
            deviceA.changeQueueDao.getPendingChanges()

        val consistent =
            finalA.name == finalB.name &&
                    finalA.quantity == finalB.quantity &&
                    finalA.isChecked == finalB.isChecked

        Log.d(
            "HIVRA_TEST",
            "FINAL DeviceA name=${finalA.name}"
        )

        Log.d(
            "HIVRA_TEST",
            "FINAL DeviceB name=${finalB.name}"
        )

        Log.d(
            "HIVRA_TEST",
            "FINAL quantity=${finalA.quantity}"
        )

        Log.d(
            "HIVRA_TEST",
            "FINAL checked=${finalA.isChecked}"
        )

        Log.d(
            "HIVRA_TEST",
            "Remaining queue after replay=${queueAfterReplay.size}"
        )

        Log.d(
            "HIVRA_TEST",
            "OFFLINE REPLAY CONSISTENT=$consistent"
        )

        check(consistent) {
            "Offline replay convergence failed"
        }

        check(queueAfterReplay.isEmpty()) {
            "Replay queue not fully drained"
        }

        Log.d(
            "HIVRA_TEST",
            "Finished OfflineReplayScenario"
        )

        Log.d(
            "HIVRA_TEST",
            "==============================================="
        )
    }
}