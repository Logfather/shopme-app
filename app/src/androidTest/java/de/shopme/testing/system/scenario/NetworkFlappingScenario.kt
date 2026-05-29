package de.shopme.testing.system.scenarios

import android.util.Log
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.testing.fake.InMemoryFakeFirestoreGateway
import de.shopme.testing.system.tests.MultiDeviceContextTest
import kotlinx.coroutines.delay
import java.util.UUID

class NetworkFlappingScenario {

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
            "Starting NetworkFlappingScenario"
        )

        // ------------------------------------------------
        // STEP 1
        // Start fully offline
        // ------------------------------------------------

        sharedGateway.isNetworkAvailable = false

        Log.d(
            "HIVRA_TEST",
            "Initial network OFFLINE"
        )

        // ------------------------------------------------
        // STEP 2
        // Generate queued updates
        // ------------------------------------------------

        val itemIds =
            mutableListOf<String>()

        repeat(50) { index ->

            val now = System.currentTimeMillis()

            val item =
                ShoppingItemEntity(
                    id = UUID.randomUUID().toString(),
                    listId = "shared-list",
                    name = "FlapItem_$index",
                    quantity = index,
                    category = "NetworkFlap",
                    isChecked = false,
                    deletedAt = null,
                    createdAt = now,
                    updatedAt = now
                )

            deviceA.roomRepository
                .createItem(item)

            itemIds.add(item.id)

            if (index % 10 == 0) {

                Log.d(
                    "HIVRA_TEST",
                    "Queued offline item=$index"
                )
            }
        }

        delay(2000)

        // ------------------------------------------------
        // STEP 3
        // Validate queue exists
        // ------------------------------------------------

        val initialPending =
            deviceA.changeQueueDao
                .getPendingChanges()

        Log.d(
            "HIVRA_TEST",
            "Initial pending=${initialPending.size}"
        )

        check(initialPending.isNotEmpty()) {
            "Queue did not accumulate during offline"
        }

        // ------------------------------------------------
        // STEP 4
        // Start replay
        // ------------------------------------------------

        deviceA.syncCoordinator
            .triggerSync(force = true)

        Log.d(
            "HIVRA_TEST",
            "Forced replay started"
        )

        // ------------------------------------------------
        // STEP 5
        // Aggressive network flapping
        // ------------------------------------------------

        repeat(20) { flap ->

            val online =
                flap % 2 == 0

            sharedGateway.isNetworkAvailable = online

            Log.d(
                "HIVRA_TEST",
                "NETWORK FLAP #$flap online=$online"
            )

            if (online) {

                deviceA.syncCoordinator
                    .triggerSync(force = true)
            }

            delay(700)
        }

        // ------------------------------------------------
        // STEP 6
        // Stabilize network
        // ------------------------------------------------

        sharedGateway.isNetworkAvailable = true

        Log.d(
            "HIVRA_TEST",
            "Network stabilized ONLINE"
        )

        deviceA.syncCoordinator
            .triggerSync(force = true)

        // ------------------------------------------------
        // STEP 7
        // Wait for queue drain
        // ------------------------------------------------

        repeat(60) {

            delay(1000)

            val remaining =
                deviceA.changeQueueDao
                    .getPendingChanges()
                    .size

            Log.d(
                "HIVRA_TEST",
                "Remaining queue=$remaining"
            )

            if (remaining == 0) {
                return@repeat
            }
        }

        // ------------------------------------------------
        // STEP 8
        // Validate queue drained
        // ------------------------------------------------

        val finalPending =
            deviceA.changeQueueDao
                .getPendingChanges()

        Log.d(
            "HIVRA_TEST",
            "Final pending=${finalPending.size}"
        )

        check(finalPending.isEmpty()) {
            "Queue failed to drain after network stabilization"
        }

        // ------------------------------------------------
        // STEP 9
        // Validate remote consistency
        // ------------------------------------------------

        var remoteConsistent = true

        itemIds.forEachIndexed { index, itemId ->

            val remote =
                sharedGateway.getItem(
                    listId = "shared-list",
                    itemId = itemId
                )

            if (remote == null) {
                remoteConsistent = false
            }

            if (index % 10 == 0) {

                Log.d(
                    "HIVRA_TEST",
                    "Validated remote item=$index"
                )
            }
        }

        Log.d(
            "HIVRA_TEST",
            "REMOTE CONSISTENT=$remoteConsistent"
        )

        check(remoteConsistent) {
            "Remote consistency failed during network flapping"
        }

        // ------------------------------------------------
        // STEP 10
        // Replicate final state to DeviceB
        // ------------------------------------------------

        itemIds.forEach { itemId ->

            val remote =
                sharedGateway.getItem(
                    listId = "shared-list",
                    itemId = itemId
                )

            if (remote != null) {
                deviceB.itemDao.upsert(remote)
            }
        }

        // ------------------------------------------------
        // STEP 11
        // Validate convergence
        // ------------------------------------------------

        val countA =
            deviceA.itemDao.getAll().size

        val countB =
            deviceB.itemDao.getAll().size

        Log.d(
            "HIVRA_TEST",
            "FINAL DeviceA count=$countA"
        )

        Log.d(
            "HIVRA_TEST",
            "FINAL DeviceB count=$countB"
        )

        val converged =
            countA == 50 &&
                    countB == 50

        Log.d(
            "HIVRA_TEST",
            "NETWORK FLAPPING CONSISTENT=$converged"
        )

        check(converged) {
            "Network flapping convergence failed"
        }

        Log.d(
            "HIVRA_TEST",
            "Finished NetworkFlappingScenario"
        )

        Log.d(
            "HIVRA_TEST",
            "==============================================="
        )
    }
}