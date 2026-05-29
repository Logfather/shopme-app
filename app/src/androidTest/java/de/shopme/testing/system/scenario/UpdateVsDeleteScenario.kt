package de.shopme.testing.system.scenarios

import android.util.Log
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.testing.system.tests.MultiDeviceContextTest
import kotlinx.coroutines.delay
import java.util.UUID

class UpdateVsDeleteScenario {

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
            "Starting UpdateVsDeleteScenario"
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
            "Initial item created"
        )

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
        // DeviceA deletes item
        // ------------------------------------------------

        val itemA =
            deviceA.itemDao.getById(initialItem.id)
                ?: error("DeviceA item missing")

        val deletedItem =
            itemA.copy(
                deletedAt = System.currentTimeMillis()
            )

        deviceA.roomRepository
            .updateItem(deletedItem)

        Log.d(
            "HIVRA_TEST",
            "DeviceA deleted item"
        )

        // ------------------------------------------------
        // STEP 4
        // DeviceB updates stale version
        // BEFORE receiving delete
        // ------------------------------------------------

        delay(50)

        val itemB =
            deviceB.itemDao.getById(initialItem.id)
                ?: error("DeviceB item missing")

        val updatedItem =
            itemB.copy(
                name = "Vollmilch"
            )

        deviceB.roomRepository
            .updateItem(updatedItem)

        Log.d(
            "HIVRA_TEST",
            "DeviceB updated stale item -> Vollmilch"
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
        // Validate DELETE authority
        // ------------------------------------------------

        val finalA =
            deviceA.itemDao.getById(initialItem.id)
                ?: error("DeviceA final item missing")

        val finalB =
            deviceB.itemDao.getById(initialItem.id)
                ?: error("DeviceB final item missing")

        val deleteWon =
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
            "DELETE AUTHORITATIVE=$deleteWon"
        )

        check(deleteWon) {
            "DELETE lost against UPDATE"
        }

        Log.d(
            "HIVRA_TEST",
            "Finished UpdateVsDeleteScenario"
        )

        Log.d(
            "HIVRA_TEST",
            "==============================================="
        )
    }
}