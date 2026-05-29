package de.shopme.testing.system.scenarios

import android.util.Log
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.testing.fake.InMemoryFakeFirestoreGateway
import de.shopme.testing.system.tests.MultiDeviceContextTest
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class RealtimeListenerOverwriteScenario {

    suspend fun run(
        deviceA: MultiDeviceContextTest,
        deviceB: MultiDeviceContextTest,
        sharedGateway: InMemoryFakeFirestoreGateway
    ) = coroutineScope {

        Log.d(
            "HIVRA_TEST",
            "==============================================="
        )

        Log.d(
            "HIVRA_TEST",
            "Starting RealtimeListenerOverwriteScenario"
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
        // Start realtime listener simulation
        // ------------------------------------------------

        val listenerJob = launch {

            sharedGateway
                .observeItems(initialItem.listId)
                .collect { remoteItems ->

                    remoteItems.forEach { remoteItem ->

                        Log.d(
                            "HIVRA_TEST",
                            "LISTENER PUSH item=${remoteItem.name} updatedAt=${remoteItem.updatedAt}"
                        )

                        deviceB.itemDao.upsert(remoteItem)
                    }
                }
        }

        delay(200)

        // ------------------------------------------------
        // STEP 4
        // DeviceA performs local update
        // ------------------------------------------------

        val updatedA =
            deviceA.itemDao.getById(initialItem.id)
                ?.copy(
                    name = "Hafermilch"
                )
                ?: error("DeviceA item missing")

        deviceA.roomRepository
            .updateItem(updatedA)

        Log.d(
            "HIVRA_TEST",
            "DeviceA local update -> Hafermilch"
        )

        // ------------------------------------------------
        // STEP 5
        // BEFORE sync settles:
        // DeviceB performs concurrent local update
        // ------------------------------------------------

        delay(100)

        val updatedB =
            deviceB.itemDao.getById(initialItem.id)
                ?.copy(
                    quantity = 5
                )
                ?: error("DeviceB item missing")

        deviceB.roomRepository
            .updateItem(updatedB)

        Log.d(
            "HIVRA_TEST",
            "DeviceB concurrent local update -> quantity=5"
        )

        // ------------------------------------------------
        // STEP 6
        // Trigger sync on both devices
        // ------------------------------------------------

        deviceA.syncCoordinator.triggerSync()
        deviceB.syncCoordinator.triggerSync()

        delay(2000)

        // ------------------------------------------------
        // STEP 7
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
        // STEP 8
        // Validate convergence
        // ------------------------------------------------

        val finalA =
            deviceA.itemDao.getById(initialItem.id)
                ?: error("DeviceA final item missing")

        val finalB =
            deviceB.itemDao.getById(initialItem.id)
                ?: error("DeviceB final item missing")

        val consistent =
            finalA.name == finalB.name &&
                    finalA.quantity == finalB.quantity &&
                    finalA.updatedAt == finalB.updatedAt

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
            "FINAL Remote name=${finalRemote.name}"
        )

        Log.d(
            "HIVRA_TEST",
            "FINAL Remote quantity=${finalRemote.quantity}"
        )

        Log.d(
            "HIVRA_TEST",
            "REALTIME CONSISTENT=$consistent"
        )

        check(consistent) {
            "Realtime listener convergence failed"
        }

        // ------------------------------------------------
        // STEP 9
        // Cleanup
        // ------------------------------------------------

        listenerJob.cancel()

        Log.d(
            "HIVRA_TEST",
            "Finished RealtimeListenerOverwriteScenario"
        )

        Log.d(
            "HIVRA_TEST",
            "==============================================="
        )
    }
}