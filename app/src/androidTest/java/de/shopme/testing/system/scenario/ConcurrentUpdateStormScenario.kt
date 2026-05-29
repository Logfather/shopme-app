package de.shopme.testing.system.scenarios

import android.util.Log
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.testing.fake.InMemoryFakeFirestoreGateway
import de.shopme.testing.system.tests.MultiDeviceContextTest
import kotlinx.coroutines.delay
import java.util.UUID

class ConcurrentUpdateStormScenario {

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
            "Starting ConcurrentUpdateStormScenario"
        )

        // ------------------------------------------------
        // STEP 1
        // Create shared item
        // ------------------------------------------------

        val now = System.currentTimeMillis()

        val sharedItem =
            ShoppingItemEntity(
                id = UUID.randomUUID().toString(),
                listId = "shared-list",
                name = "Milk",
                quantity = 1,
                category = "Storm",
                isChecked = false,
                deletedAt = null,
                createdAt = now,
                updatedAt = now
            )

        deviceA.roomRepository
            .createItem(sharedItem)

        delay(1500)

        Log.d(
            "HIVRA_TEST",
            "Initial shared item created"
        )

        // ------------------------------------------------
        // STEP 2
        // Replicate initial state to DeviceB
        // ------------------------------------------------

        val initialRemote =
            sharedGateway.getItem(
                listId = sharedItem.listId,
                itemId = sharedItem.id
            )
                ?: error("Initial remote item missing")

        deviceB.itemDao.upsert(initialRemote)

        Log.d(
            "HIVRA_TEST",
            "DeviceB received initial state"
        )

        // ------------------------------------------------
        // STEP 3
        // Concurrent update storm
        // ------------------------------------------------

        repeat(20) { iteration ->

            // -----------------------------
            // DEVICE A UPDATE
            // -----------------------------

            val localA =
                deviceA.itemDao.getById(sharedItem.id)
                    ?: error("DeviceA item missing")

            val updatedA =
                localA.copy(
                    name = "A_Update_$iteration",
                    quantity = iteration,
                    updatedAt = System.currentTimeMillis()
                )

            deviceA.roomRepository
                .updateItem(updatedA)

            Log.d(
                "HIVRA_TEST",
                "DeviceA update iteration=$iteration name=${updatedA.name}"
            )

            delay(50)

            // -----------------------------
            // DEVICE B UPDATE
            // -----------------------------

            val localB =
                deviceB.itemDao.getById(sharedItem.id)
                    ?: error("DeviceB item missing")

            val updatedB =
                localB.copy(
                    name = "B_Update_$iteration",
                    quantity = iteration * 10,
                    updatedAt = System.currentTimeMillis()
                )

            deviceB.roomRepository
                .updateItem(updatedB)

            Log.d(
                "HIVRA_TEST",
                "DeviceB update iteration=$iteration name=${updatedB.name}"
            )

            delay(150)
        }

        // ------------------------------------------------
        // STEP 4
        // Force replay convergence
        // ------------------------------------------------

        deviceA.syncCoordinator
            .triggerSync(force = true)

        deviceB.syncCoordinator
            .triggerSync(force = true)

        Log.d(
            "HIVRA_TEST",
            "Forced replay convergence started"
        )

        // ------------------------------------------------
        // STEP 5
        // Wait for queues to drain
        // ------------------------------------------------

        repeat(60) {

            delay(1000)

            val pendingA =
                deviceA.changeQueueDao
                    .getPendingChanges()
                    .size

            val pendingB =
                deviceB.changeQueueDao
                    .getPendingChanges()
                    .size

            Log.d(
                "HIVRA_TEST",
                "Queue state DeviceA=$pendingA DeviceB=$pendingB"
            )

            if (
                pendingA == 0 &&
                pendingB == 0
            ) {
                return@repeat
            }
        }

        // ------------------------------------------------
        // STEP 6
        // Pull final remote state
        // ------------------------------------------------

        val finalRemote =
            sharedGateway.getItem(
                listId = sharedItem.listId,
                itemId = sharedItem.id
            )
                ?: error("Final remote item missing")

        deviceA.itemDao.upsert(finalRemote)
        deviceB.itemDao.upsert(finalRemote)

        delay(1000)

        // ------------------------------------------------
        // STEP 7
        // Validate convergence
        // ------------------------------------------------

        val finalA =
            deviceA.itemDao.getById(sharedItem.id)
                ?: error("Final DeviceA item missing")

        val finalB =
            deviceB.itemDao.getById(sharedItem.id)
                ?: error("Final DeviceB item missing")

        val queueA =
            deviceA.changeQueueDao
                .getPendingChanges()

        val queueB =
            deviceB.changeQueueDao
                .getPendingChanges()

        val converged =
            finalA.name == finalB.name &&
                    finalA.quantity == finalB.quantity &&
                    finalA.updatedAt == finalB.updatedAt

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
            "FINAL DeviceA quantity=${finalA.quantity}"
        )

        Log.d(
            "HIVRA_TEST",
            "FINAL DeviceB quantity=${finalB.quantity}"
        )

        Log.d(
            "HIVRA_TEST",
            "FINAL DeviceA updatedAt=${finalA.updatedAt}"
        )

        Log.d(
            "HIVRA_TEST",
            "FINAL DeviceB updatedAt=${finalB.updatedAt}"
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
            "UPDATE STORM CONVERGED=$converged"
        )

        check(converged) {
            "Concurrent update storm failed to converge"
        }

        check(queueA.isEmpty()) {
            "DeviceA queue not drained"
        }

        check(queueB.isEmpty()) {
            "DeviceB queue not drained"
        }

        Log.d(
            "HIVRA_TEST",
            "Finished ConcurrentUpdateStormScenario"
        )

        Log.d(
            "HIVRA_TEST",
            "==============================================="
        )
    }
}