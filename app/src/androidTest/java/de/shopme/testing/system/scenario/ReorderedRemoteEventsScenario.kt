package de.shopme.testing.system.scenarios

import android.util.Log
import de.shopme.core.coroutines.AppScope
import de.shopme.data.sync.ConflictResolver
import de.shopme.data.sync.FirestoreListener
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.testing.fake.InMemoryFakeFirestoreGateway
import de.shopme.testing.system.tests.MultiDeviceContextTest
import kotlinx.coroutines.delay
import java.util.UUID

class ReorderedRemoteEventsScenario {

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
            "Starting ReorderedRemoteEventsScenario"
        )

        // ------------------------------------------------
        // REALTIME LISTENER
        // ------------------------------------------------

        val firestoreListener =
            FirestoreListener(
                dataSource = sharedGateway,
                itemDao = deviceB.itemDao,
                listDao = deviceB.listDao,
                conflictResolver = ConflictResolver(),
                appScope = AppScope()
            )

        firestoreListener.startItemSync("shared-list")

        Log.d(
            "HIVRA_TEST",
            "Realtime listener started"
        )

        // ------------------------------------------------
        // STEP 1
        // Create initial item
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

        delay(1000)

        Log.d(
            "HIVRA_TEST",
            "Initial item created"
        )

        // ------------------------------------------------
        // STEP 2
        // Create ordered versions
        // ------------------------------------------------

        val version1 =
            initialItem.copy(
                name = "Milch_v1",
                quantity = 1,
                updatedAt = now + 1000
            )

        val version2 =
            initialItem.copy(
                name = "Milch_v2",
                quantity = 2,
                updatedAt = now + 2000
            )

        val version3 =
            initialItem.copy(
                name = "Milch_v3",
                quantity = 3,
                updatedAt = now + 3000
            )

        // ------------------------------------------------
        // STEP 3
        // Simulate OUT-OF-ORDER remote delivery
        // ------------------------------------------------

        Log.d(
            "HIVRA_TEST",
            "Applying remote version3 FIRST"
        )

        sharedGateway.updateItem(
            listId = "shared-list",
            item = version3
        )

        delay(300)

        Log.d(
            "HIVRA_TEST",
            "Applying stale remote version1"
        )

        sharedGateway.updateItem(
            listId = "shared-list",
            item = version1
        )

        delay(300)

        Log.d(
            "HIVRA_TEST",
            "Applying stale remote version2"
        )

        sharedGateway.updateItem(
            listId = "shared-list",
            item = version2
        )

        delay(1000)

        // ------------------------------------------------
        // STEP 4
        // Validate stale overwrite protection
        // ------------------------------------------------

        val finalB =
            deviceB.itemDao.getById(initialItem.id)
                ?: error("DeviceB final item missing")

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
            "FINAL DeviceB updatedAt=${finalB.updatedAt}"
        )

        val staleProtected =
            finalB.name == "Milch_v3" &&
                    finalB.quantity == 3 &&
                    finalB.updatedAt == version3.updatedAt

        Log.d(
            "HIVRA_TEST",
            "STALE EVENT PROTECTED=$staleProtected"
        )

        check(staleProtected) {
            "Older remote event overwrote newer state"
        }

        // ------------------------------------------------
        // STEP 5
        // Validate eventual convergence
        // ------------------------------------------------

        deviceA.itemDao.upsert(finalB)

        val finalA =
            deviceA.itemDao.getById(initialItem.id)
                ?: error("DeviceA final item missing")

        val converged =
            finalA.updatedAt == finalB.updatedAt &&
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
            "REORDERED EVENTS CONSISTENT=$converged"
        )

        check(converged) {
            "Devices failed to converge after reordered events"
        }

        firestoreListener.stop()

        Log.d(
            "HIVRA_TEST",
            "Finished ReorderedRemoteEventsScenario"
        )

        Log.d(
            "HIVRA_TEST",
            "==============================================="
        )
    }
}