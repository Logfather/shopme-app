package de.shopme.testing.system.scenarios

import android.util.Log
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.testing.fake.InMemoryFakeFirestoreGateway
import de.shopme.testing.system.tests.MultiDeviceContextTest
import kotlinx.coroutines.delay
import java.util.UUID

class StaleDeviceReconnectAfterLongOfflineScenario {

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
            "Starting StaleDeviceReconnectAfterLongOfflineScenario"
        )

        // ------------------------------------------------
        // STEP 1
        // Create shared item
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
        // DeviceB goes OFFLINE for long time
        // ------------------------------------------------

        sharedGateway.isNetworkAvailable = false

        Log.d(
            "HIVRA_TEST",
            "DeviceB simulated LONG OFFLINE"
        )

        // ------------------------------------------------
        // STEP 4
        // DeviceA continues evolving remote state
        // ------------------------------------------------

        sharedGateway.isNetworkAvailable = true

        repeat(5) { index ->

            val current =
                deviceA.itemDao.getById(initialItem.id)
                    ?: error("DeviceA item missing")

            val updated =
                current.copy(
                    name = "Milch_v$index",
                    quantity = index + 1,
                    updatedAt = System.currentTimeMillis()
                )

            deviceA.roomRepository
                .updateItem(updated)

            Log.d(
                "HIVRA_TEST",
                "DeviceA update #$index -> ${updated.name}"
            )

            delay(400)
        }

        // ------------------------------------------------
        // STEP 5
        // DeviceA performs DELETE while B still stale
        // ------------------------------------------------

        val latestA =
            deviceA.itemDao.getById(initialItem.id)
                ?: error("Latest DeviceA item missing")

        val deleted =
            latestA.copy(
                deletedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

        deviceA.roomRepository
            .softDeleteItem(latestA.id)

        Log.d(
            "HIVRA_TEST",
            "DeviceA DELETE while DeviceB offline"
        )

        delay(1000)

        // ------------------------------------------------
        // STEP 6
        // DeviceB still has OLD stale version
        // ------------------------------------------------

        val staleB =
            deviceB.itemDao.getById(initialItem.id)
                ?: error("DeviceB stale item missing")

        Log.d(
            "HIVRA_TEST",
            "DeviceB stale state name=${staleB.name} quantity=${staleB.quantity} deletedAt=${staleB.deletedAt}"
        )

        // ------------------------------------------------
        // STEP 7
        // DeviceB modifies stale item locally
        // ------------------------------------------------

        val staleModified =
            staleB.copy(
                name = "STALE_DEVICE_UPDATE",
                quantity = 99,
                updatedAt = System.currentTimeMillis()
            )

        deviceB.roomRepository
            .updateItem(staleModified)

        Log.d(
            "HIVRA_TEST",
            "DeviceB stale local update queued"
        )

        delay(500)

        // ------------------------------------------------
        // STEP 8
        // Reconnect stale device
        // ------------------------------------------------

        sharedGateway.isNetworkAvailable = true

        deviceB.changeQueueDao
            .resetRetryBackoff()

        Log.d(
            "HIVRA_TEST",
            "DeviceB reconnects after long offline"
        )

        // ------------------------------------------------
        // STEP 9
        // Trigger replay from stale device
        // ------------------------------------------------

        deviceB.syncCoordinator
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

        deviceA.itemDao.upsert(finalRemote)
        deviceB.itemDao.upsert(finalRemote)

        // ------------------------------------------------
        // STEP 11
        // Validate stale replay protection
        // ------------------------------------------------

        val finalA =
            deviceA.itemDao.getById(initialItem.id)
                ?: error("DeviceA final missing")

        val finalB =
            deviceB.itemDao.getById(initialItem.id)
                ?: error("DeviceB final missing")

        val staleRejected =
            finalA.deletedAt != null &&
                    finalB.deletedAt != null &&
                    finalRemote.deletedAt != null

        Log.d(
            "HIVRA_TEST",
            "FINAL DeviceA deletedAt=${finalA.deletedAt}"
        )

        Log.d(
            "HIVRA_TEST",
            "FINAL DeviceB deletedAt=${finalB.deletedAt}"
        )

        Log.d(
            "HIVRA_TEST",
            "FINAL Remote deletedAt=${finalRemote.deletedAt}"
        )

        Log.d(
            "HIVRA_TEST",
            "STALE REPLAY REJECTED=$staleRejected"
        )

        check(staleRejected) {
            "Stale device resurrected deleted entity"
        }

        // ------------------------------------------------
        // STEP 12
        // Validate queue convergence
        // ------------------------------------------------

        val queueA =
            deviceA.changeQueueDao.getPendingChanges()

        val queueB =
            deviceB.changeQueueDao.getPendingChanges()

        Log.d(
            "HIVRA_TEST",
            "Remaining queue DeviceA=${queueA.size}"
        )

        Log.d(
            "HIVRA_TEST",
            "Remaining queue DeviceB=${queueB.size}"
        )

        check(queueA.isEmpty()) {
            "DeviceA queue not drained"
        }

        check(queueB.isEmpty()) {
            "DeviceB queue not drained"
        }

        Log.d(
            "HIVRA_TEST",
            "Finished StaleDeviceReconnectAfterLongOfflineScenario"
        )

        Log.d(
            "HIVRA_TEST",
            "==============================================="
        )
    }
}