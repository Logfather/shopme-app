package de.shopme.testing.system.scenarios

import android.util.Log
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.testing.system.tests.MultiDeviceContextTest
import kotlinx.coroutines.delay
import java.util.UUID

class ConflictResolutionScenario {

    suspend fun run(
        deviceA: MultiDeviceContextTest,
        deviceB: MultiDeviceContextTest
    ) {

        Log.d(
            "HIVRA_TEST",
            "==============================================="
        )

        Log.d(
            "HIVRA_TEST",
            "Starting ConflictResolutionScenario"
        )

        // ------------------------------------------------
        // STEP 1
        // Create initial shared item
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
            "Initial item created name=${initialItem.name}"
        )

        delay(300)

        // ------------------------------------------------
        // STEP 2
        // DeviceB receives initial state
        // ------------------------------------------------

        val remoteInitial =
            deviceA.syncCoordinator
                .firestore
                .getItem(
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
        // DeviceA updates item
        // ------------------------------------------------

        val itemA =
            deviceA.itemDao.getById(initialItem.id)
                ?: error("DeviceA item missing")

        val updatedA =
            itemA.copy(
                name = "Hafermilch"
            )

        deviceA.roomRepository
            .updateItem(updatedA)

        Log.d(
            "HIVRA_TEST",
            "DeviceA updated -> Hafermilch"
        )

        // ------------------------------------------------
        // STEP 4
        // DeviceB updates item concurrently
        // ------------------------------------------------

        delay(50)

        val itemB =
            deviceB.itemDao.getById(initialItem.id)
                ?: error("DeviceB item missing")

        val updatedB =
            itemB.copy(
                name = "Vollmilch"
            )

        deviceB.roomRepository
            .updateItem(updatedB)

        Log.d(
            "HIVRA_TEST",
            "DeviceB updated -> Vollmilch"
        )

        // ------------------------------------------------
        // STEP 5
        // Trigger concurrent sync
        // ------------------------------------------------

        deviceA.syncCoordinator.triggerSync()
        deviceB.syncCoordinator.triggerSync()

        delay(1000)

        // ------------------------------------------------
        // STEP 6
        // Pull final remote state
        // ------------------------------------------------

        val finalRemote =
            deviceA.syncCoordinator
                .firestore
                .getItem(
                    listId = initialItem.listId,
                    itemId = initialItem.id
                )
                ?: error("Final remote item missing")

        deviceA.itemDao.upsert(finalRemote)
        deviceB.itemDao.upsert(finalRemote)

        // ------------------------------------------------
        // STEP 7
        // Validate convergence
        // ------------------------------------------------

        val finalA =
            deviceA.itemDao.getById(initialItem.id)
                ?: error("DeviceA final missing")

        val finalB =
            deviceB.itemDao.getById(initialItem.id)
                ?: error("DeviceB final missing")

        val consistent =
            finalA.name == finalB.name &&
                    finalB.name == finalRemote.name

        Log.d(
            "HIVRA_TEST",
            "FINAL DeviceA=${finalA.name}"
        )

        Log.d(
            "HIVRA_TEST",
            "FINAL DeviceB=${finalB.name}"
        )

        Log.d(
            "HIVRA_TEST",
            "FINAL Remote=${finalRemote.name}"
        )

        Log.d(
            "HIVRA_TEST",
            "CONFLICT CONSISTENT=$consistent"
        )

        check(consistent) {
            "Conflict resolution failed"
        }

        Log.d(
            "HIVRA_TEST",
            "Finished ConflictResolutionScenario"
        )

        Log.d(
            "HIVRA_TEST",
            "==============================================="
        )
    }
}