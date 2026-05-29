package de.shopme.testing.system.scenarios

import android.util.Log
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.testing.fake.InMemoryFakeFirestoreGateway
import de.shopme.testing.system.tests.MultiDeviceContextTest
import kotlinx.coroutines.delay
import java.util.UUID

class ConcurrentDeleteStormScenario {

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
            "Starting ConcurrentDeleteStormScenario"
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
                name = "Milch",
                quantity = 1,
                category = "Dairy",
                isChecked = false,
                deletedAt = null,
                createdAt = now,
                updatedAt = now
            )

        deviceA.roomRepository
            .createItem(sharedItem)

        delay(1000)

        Log.d(
            "HIVRA_TEST",
            "Shared item created"
        )

        // ------------------------------------------------
        // STEP 2
        // Replicate to DeviceB
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
            "DeviceB received shared item"
        )

        // ------------------------------------------------
        // STEP 3
        // Concurrent delete storm
        // ------------------------------------------------

        repeat(5) { index ->

            val localA =
                deviceA.itemDao.getById(sharedItem.id)
                    ?: error("DeviceA item missing")

            val localB =
                deviceB.itemDao.getById(sharedItem.id)
                    ?: error("DeviceB item missing")

            deviceA.roomRepository
                .softDeleteItem(localA.id)

            deviceB.roomRepository
                .softDeleteItem(localB.id)

            Log.d(
                "HIVRA_TEST",
                "Concurrent delete iteration=$index"
            )

            delay(300)
        }

        // ------------------------------------------------
        // STEP 4
        // Force replay convergence
        // ------------------------------------------------

        deviceA.syncCoordinator
            .triggerSync(force = true)

        deviceB.syncCoordinator
            .triggerSync(force = true)

        delay(3000)

        // ------------------------------------------------
        // STEP 5
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

        // ------------------------------------------------
        // STEP 6
        // Validate convergence
        // ------------------------------------------------

        val finalA =
            deviceA.itemDao.getById(sharedItem.id)
                ?: error("DeviceA final item missing")

        val finalB =
            deviceB.itemDao.getById(sharedItem.id)
                ?: error("DeviceB final item missing")

        val queueA =
            deviceA.changeQueueDao
                .getPendingChanges()

        val queueB =
            deviceB.changeQueueDao
                .getPendingChanges()

        val deletedConsistent =
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
            "Remaining queue DeviceA=${queueA.size}"
        )

        Log.d(
            "HIVRA_TEST",
            "Remaining queue DeviceB=${queueB.size}"
        )

        Log.d(
            "HIVRA_TEST",
            "DELETE STORM CONSISTENT=$deletedConsistent"
        )

        check(deletedConsistent) {
            "Concurrent delete storm failed to converge"
        }

        check(queueA.isEmpty()) {
            "DeviceA queue not drained"
        }

        check(queueB.isEmpty()) {
            "DeviceB queue not drained"
        }

        Log.d(
            "HIVRA_TEST",
            "Finished ConcurrentDeleteStormScenario"
        )

        Log.d(
            "HIVRA_TEST",
            "==============================================="
        )
    }
}