package de.shopme.testing.system.scenarios

import android.util.Log
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.testing.fake.InMemoryFakeFirestoreGateway
import de.shopme.testing.system.tests.MultiDeviceContextTest
import kotlinx.coroutines.delay
import java.util.UUID

class AppRestartDuringPendingSyncScenario {

    suspend fun run(
        deviceA: MultiDeviceContextTest,
        deviceB: MultiDeviceContextTest,
        sharedGateway: InMemoryFakeFirestoreGateway,
        recreateDeviceA: suspend () -> MultiDeviceContextTest
    ) {

        Log.d(
            "HIVRA_TEST",
            "==============================================="
        )

        Log.d(
            "HIVRA_TEST",
            "Starting AppRestartDuringPendingSyncScenario"
        )

        // ------------------------------------------------
        // STEP 1
        // Create initial item online
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
        // DeviceA performs offline update
        // ------------------------------------------------

        val updatedOffline =
            deviceA.itemDao.getById(initialItem.id)
                ?.copy(
                    name = "Hafermilch",
                    quantity = 3
                )
                ?: error("Offline item missing")

        deviceA.roomRepository
            .updateItem(updatedOffline)

        Log.d(
            "HIVRA_TEST",
            "Offline update queued"
        )

        delay(1000)

        // ------------------------------------------------
        // STEP 5
        // Validate queue exists BEFORE restart
        // ------------------------------------------------

        val queueBeforeRestart =
            deviceA.changeQueueDao.getPendingChanges()

        Log.d(
            "HIVRA_TEST",
            "Queue before restart size=${queueBeforeRestart.size}"
        )

        check(queueBeforeRestart.isNotEmpty()) {
            "Expected pending queue before restart"
        }

        // ------------------------------------------------
        // STEP 6
        // Simulate APP RESTART
        // ------------------------------------------------

        Log.d(
            "HIVRA_TEST",
            "Simulating APP RESTART"
        )

        val restartedDeviceA =
            recreateDeviceA()

        Log.d(
            "HIVRA_TEST",
            "DeviceA recreated"
        )

        // ------------------------------------------------
        // STEP 7
        // Validate queue survived restart
        // ------------------------------------------------

        val queueAfterRestart =
            restartedDeviceA
                .changeQueueDao
                .getPendingChanges()

        Log.d(
            "HIVRA_TEST",
            "Queue after restart size=${queueAfterRestart.size}"
        )

        check(queueAfterRestart.isNotEmpty()) {
            "Queue lost after restart"
        }

        // ------------------------------------------------
        // STEP 8
        // Restore network
        // ------------------------------------------------

        sharedGateway.isNetworkAvailable = true

        restartedDeviceA
            .changeQueueDao
            .resetRetryBackoff()

        Log.d(
            "HIVRA_TEST",
            "Simulated RECONNECT"
        )

        // ------------------------------------------------
        // STEP 9
        // Resume sync after restart
        // ------------------------------------------------

        restartedDeviceA
            .syncCoordinator
            .triggerSync()

        delay(2000)

        // ------------------------------------------------
        // STEP 10
        // Pull final remote state
        // ------------------------------------------------

        val finalRemote =
            sharedGateway.getItem(
                listId = initialItem.listId,
                itemId = initialItem.id
            )
                ?: error("Final remote item missing")

        restartedDeviceA
            .itemDao
            .upsert(finalRemote)

        deviceB
            .itemDao
            .upsert(finalRemote)

        // ------------------------------------------------
        // STEP 11
        // Validate convergence
        // ------------------------------------------------

        val finalA =
            restartedDeviceA
                .itemDao
                .getById(initialItem.id)
                ?: error("Restarted DeviceA final item missing")

        val finalB =
            deviceB
                .itemDao
                .getById(initialItem.id)
                ?: error("DeviceB final item missing")

        val remainingQueue =
            restartedDeviceA
                .changeQueueDao
                .getPendingChanges()

        val consistent =
            finalA.name == finalB.name &&
                    finalA.quantity == finalB.quantity

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
            "Remaining queue after restart replay=${remainingQueue.size}"
        )

        Log.d(
            "HIVRA_TEST",
            "APP RESTART CONSISTENT=$consistent"
        )

        check(consistent) {
            "App restart convergence failed"
        }

        check(remainingQueue.isEmpty()) {
            "Queue not drained after restart replay"
        }

        Log.d(
            "HIVRA_TEST",
            "Finished AppRestartDuringPendingSyncScenario"
        )

        Log.d(
            "HIVRA_TEST",
            "==============================================="
        )
    }
}