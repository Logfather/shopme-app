package de.shopme.testing.system.scenarios

import android.util.Log
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.testing.fake.InMemoryFakeFirestoreGateway
import de.shopme.testing.system.tests.MultiDeviceContextTest
import kotlinx.coroutines.delay
import java.util.UUID

class LargeQueueReplayScenario {

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
            "Starting LargeQueueReplayScenario"
        )

        // ------------------------------------------------
        // STEP 1
        // Simulate FULL OFFLINE
        // ------------------------------------------------

        sharedGateway.isNetworkAvailable = false

        Log.d(
            "HIVRA_TEST",
            "Network disabled"
        )

        // ------------------------------------------------
        // STEP 2
        // Generate LARGE pending queue
        // ------------------------------------------------

        val itemCount = 250

        repeat(itemCount) { index ->

            val now = System.currentTimeMillis()

            val item =
                ShoppingItemEntity(
                    id = UUID.randomUUID().toString(),
                    listId = "shared-list",
                    name = "Item_$index",
                    quantity = index,
                    category = "StressTest",
                    isChecked = false,
                    deletedAt = null,
                    createdAt = now,
                    updatedAt = now
                )

            deviceA.roomRepository
                .createItem(item)

            if (index % 25 == 0) {

                Log.d(
                    "HIVRA_TEST",
                    "Generated queue item=$index"
                )
            }
        }

        delay(3000)

        // ------------------------------------------------
        // STEP 3
        // Validate queue accumulation
        // ------------------------------------------------

        val pendingBeforeReplay =
            deviceA.changeQueueDao
                .getPendingChanges()

        Log.d(
            "HIVRA_TEST",
            "Pending before replay=${pendingBeforeReplay.size}"
        )

        check(
            pendingBeforeReplay.isNotEmpty()
        ) {
            "Large queue was not generated"
        }

        // ------------------------------------------------
        // STEP 4
        // Restore network
        // ------------------------------------------------

        sharedGateway.isNetworkAvailable = true

        Log.d(
            "HIVRA_TEST",
            "Network restored"
        )

        // ------------------------------------------------
        // STEP 5
        // Force replay
        // ------------------------------------------------

        val replayStart =
            System.currentTimeMillis()

        deviceA.syncCoordinator
            .triggerSync(force = true)

        Log.d(
            "HIVRA_TEST",
            "Forced replay started"
        )

        // ------------------------------------------------
        // STEP 6
        // Wait for replay drain
        // ------------------------------------------------

        repeat(60) {

            delay(1000)

            val remaining =
                deviceA.changeQueueDao
                    .getPendingChanges()
                    .size

            Log.d(
                "HIVRA_TEST",
                "Replay remaining queue=$remaining"
            )

            if (remaining == 0) {
                return@repeat
            }
        }

        val replayEnd =
            System.currentTimeMillis()

        val replayDuration =
            replayEnd - replayStart

        // ------------------------------------------------
        // STEP 7
        // Validate queue drained
        // ------------------------------------------------

        val finalPending =
            deviceA.changeQueueDao
                .getPendingChanges()

        Log.d(
            "HIVRA_TEST",
            "Final pending queue=${finalPending.size}"
        )

        check(
            finalPending.isEmpty()
        ) {
            "Replay queue not fully drained"
        }

        // ------------------------------------------------
        // STEP 8
        // Validate remote consistency
        // ------------------------------------------------

        var remoteConsistent = true

        repeat(itemCount) { index ->

            val remote =
                sharedGateway.getItem(
                    listId = "shared-list",
                    itemId = pendingBeforeReplay[index].entityId
                )

            if (remote == null) {
                remoteConsistent = false
            }
        }

        Log.d(
            "HIVRA_TEST",
            "REMOTE CONSISTENT=$remoteConsistent"
        )

        check(remoteConsistent) {
            "Remote replay consistency failed"
        }

        // ------------------------------------------------
        // STEP 9
        // Replicate final remote state to DeviceB
        // ------------------------------------------------

        pendingBeforeReplay.forEachIndexed { index, change ->

            val remote =
                sharedGateway.getItem(
                    listId = "shared-list",
                    itemId = change.entityId
                )

            if (remote != null) {
                deviceB.itemDao.upsert(remote)
            }

            if (index % 50 == 0) {

                Log.d(
                    "HIVRA_TEST",
                    "Replicated remote item=$index"
                )
            }
        }

        // ------------------------------------------------
        // STEP 10
        // Validate convergence
        // ------------------------------------------------

        val localCountA =
            deviceA.itemDao.getAll().size

        val localCountB =
            deviceB.itemDao.getAll().size

        Log.d(
            "HIVRA_TEST",
            "FINAL DeviceA count=$localCountA"
        )

        Log.d(
            "HIVRA_TEST",
            "FINAL DeviceB count=$localCountB"
        )

        Log.d(
            "HIVRA_TEST",
            "REPLAY DURATION MS=$replayDuration"
        )

        val converged =
            localCountA == itemCount &&
                    localCountB == itemCount

        Log.d(
            "HIVRA_TEST",
            "LARGE REPLAY CONSISTENT=$converged"
        )

        check(converged) {
            "Large queue replay failed to converge"
        }

        Log.d(
            "HIVRA_TEST",
            "Finished LargeQueueReplayScenario"
        )

        Log.d(
            "HIVRA_TEST",
            "==============================================="
        )
    }
}