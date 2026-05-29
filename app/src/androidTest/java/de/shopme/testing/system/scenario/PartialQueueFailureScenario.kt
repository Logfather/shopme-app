package de.shopme.testing.system.scenarios

import android.util.Log
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.testing.fake.InMemoryFakeFirestoreGateway
import de.shopme.testing.system.tests.MultiDeviceContextTest
import kotlinx.coroutines.delay
import java.util.UUID

class PartialQueueFailureScenario {

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
            "Starting PartialQueueFailureScenario"
        )

        // ------------------------------------------------
        // STEP 1
        // Simulate offline BEFORE queue processing
        // ------------------------------------------------

        sharedGateway.isNetworkAvailable = false

        Log.d(
            "HIVRA_TEST",
            "Network disabled"
        )

        // ------------------------------------------------
        // STEP 2
        // Create Item A
        // ------------------------------------------------

        val now = System.currentTimeMillis()

        val itemA =
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
            .createItem(itemA)

        Log.d(
            "HIVRA_TEST",
            "ItemA queued"
        )

        // ------------------------------------------------
        // STEP 3
        // Create Item B
        // ------------------------------------------------

        val itemB =
            ShoppingItemEntity(
                id = UUID.randomUUID().toString(),
                listId = "shared-list",
                name = "Brot",
                quantity = 2,
                category = "Bakery",
                isChecked = false,
                deletedAt = null,
                createdAt = now,
                updatedAt = now
            )

        deviceA.roomRepository
            .createItem(itemB)

        Log.d(
            "HIVRA_TEST",
            "ItemB queued"
        )

        // ------------------------------------------------
        // STEP 4
        // Let queue attempt processing
        // ------------------------------------------------

        delay(2000)

        // ------------------------------------------------
        // STEP 5
        // Validate queue blocked correctly
        // ------------------------------------------------

        val pendingBeforeReconnect =
            deviceA.changeQueueDao
                .getPendingChanges()

        Log.d(
            "HIVRA_TEST",
            "Pending before reconnect=${pendingBeforeReconnect.size}"
        )

        check(pendingBeforeReconnect.isNotEmpty()) {
            "Queue unexpectedly empty during offline failure"
        }

        // ------------------------------------------------
        // STEP 6
        // Re-enable network
        // ------------------------------------------------

        sharedGateway.isNetworkAvailable = true

        Log.d(
            "HIVRA_TEST",
            "Network re-enabled"
        )

        // ------------------------------------------------
        // STEP 7
        // Trigger replay
        // ------------------------------------------------

        deviceA.syncCoordinator
            .triggerSync(force = true)

        delay(3000)

        // ------------------------------------------------
        // STEP 8
        // Pull final remote state
        // ------------------------------------------------

        val remoteA =
            sharedGateway.getItem(
                listId = itemA.listId,
                itemId = itemA.id
            )

        val remoteB =
            sharedGateway.getItem(
                listId = itemB.listId,
                itemId = itemB.id
            )

        check(remoteA != null) {
            "Remote ItemA missing after replay"
        }

        check(remoteB != null) {
            "Remote ItemB missing after replay"
        }

        deviceB.itemDao.upsert(remoteA)
        deviceB.itemDao.upsert(remoteB)

        // ------------------------------------------------
        // STEP 9
        // Validate final convergence
        // ------------------------------------------------

        val finalA =
            deviceB.itemDao.getById(itemA.id)
                ?: error("DeviceB ItemA missing")

        val finalB =
            deviceB.itemDao.getById(itemB.id)
                ?: error("DeviceB ItemB missing")

        val queueA =
            deviceA.changeQueueDao
                .getPendingChanges()

        val queueB =
            deviceB.changeQueueDao
                .getPendingChanges()

        val consistent =
            finalA.name == "Milch" &&
                    finalB.name == "Brot"

        Log.d(
            "HIVRA_TEST",
            "FINAL ItemA=${finalA.name}"
        )

        Log.d(
            "HIVRA_TEST",
            "FINAL ItemB=${finalB.name}"
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
            "PARTIAL FAILURE CONSISTENT=$consistent"
        )

        check(consistent) {
            "Partial queue replay corrupted final state"
        }

        check(queueA.isEmpty()) {
            "DeviceA queue not drained"
        }

        check(queueB.isEmpty()) {
            "DeviceB queue not drained"
        }

        Log.d(
            "HIVRA_TEST",
            "Finished PartialQueueFailureScenario"
        )

        Log.d(
            "HIVRA_TEST",
            "==============================================="
        )
    }
}